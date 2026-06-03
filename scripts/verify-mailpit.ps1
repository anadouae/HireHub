# Vérifie Mailpit + publie un événement test candidature.statut.changed
$ErrorActionPreference = "Stop"

Write-Host "=== HireHub — vérification Mailpit ===" -ForegroundColor Cyan

try {
    $mailpit = Invoke-RestMethod -Uri "http://localhost:8025/api/v1/messages" -TimeoutSec 5
    Write-Host "Mailpit OK — messages actuels: $($mailpit.total)" -ForegroundColor Green
} catch {
    Write-Host "Mailpit inaccessible sur http://localhost:8025 — lancez: docker compose up -d mailpit" -ForegroundColor Red
    exit 1
}

$ports = @{ Email = 8093; Rabbit = 5672; Candidature = 8083 }
foreach ($p in $ports.GetEnumerator()) {
    $listen = Get-NetTCPConnection -LocalPort $p.Value -State Listen -ErrorAction SilentlyContinue
    if ($listen) {
        Write-Host "$($p.Key) (port $($p.Value)): OK" -ForegroundColor Green
    } else {
        Write-Host "$($p.Key) (port $($p.Value)): ARRETÉ — redémarrez avec scripts/start-all.ps1" -ForegroundColor Yellow
    }
}

$payload = @{
    eventId = "verify-" + [guid]::NewGuid().ToString()
    eventType = "CANDIDATURE_STATUT_CHANGED"
    recipientEmail = "test-candidat@hirehub.local"
    recipientName = "Test Candidat"
    payload = @{
        offerTitle = "Offre test Mailpit"
        oldStatus = "SOUMISE"
        newStatus = "EN_COURS"
        comment = ""
    }
} | ConvertTo-Json -Compress -Depth 5

$body = @{
    properties = @{ content_type = "application/json"; delivery_mode = 2 }
    routing_key = "candidature.statut.changed"
    payload = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($payload))
    payload_encoding = "base64"
} | ConvertTo-Json -Depth 5

$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("hirehub:hirehub"))
Invoke-RestMethod -Uri "http://localhost:15672/api/exchanges/%2F/hirehub.events/publish" `
    -Method Post -Headers @{ Authorization = "Basic $auth" } -ContentType "application/json" -Body $body | Out-Null

Write-Host "Événement test publié — attente 3 s..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

$after = Invoke-RestMethod -Uri "http://localhost:8025/api/v1/messages" -TimeoutSec 5
if ($after.total -gt $mailpit.total) {
    Write-Host "SUCCÈS — nouveau message dans Mailpit (total: $($after.total))" -ForegroundColor Green
    Write-Host "Ouvrez http://localhost:8025" -ForegroundColor Green
} else {
    Write-Host "ÉCHEC — aucun nouveau mail. Redémarrez email-service (8093) et event-service après mvn compile." -ForegroundColor Red
}
