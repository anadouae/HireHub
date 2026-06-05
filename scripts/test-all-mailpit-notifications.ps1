# Test Mailpit — publie un EmailEventDTO par type de notification RabbitMQ
# Prérequis : docker (mailpit + rabbitmq) + email-service (8093) avec MAIL_HOST=localhost MAIL_PORT=1025

param(
    [string]$MailpitApi = "http://localhost:8025/api/v1",
    [string]$RabbitMgmt = "http://localhost:15672/api",
    [string]$RabbitUser = "hirehub",
    [string]$RabbitPass = "hirehub",
    [string]$TestEmail = "mailpit-test@hirehub.local"
)

$ErrorActionPreference = "Stop"
$Exchange = "hirehub.events"
$TypeId = "com.hirehub.common.notification.EmailEventDTO"
$AuthHeader = @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${RabbitUser}:${RabbitPass}")) }

function Test-MailpitUp {
    try {
        Invoke-RestMethod -Uri "$MailpitApi/messages" -Method Get | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Get-MailCount {
    return [int](Invoke-RestMethod -Uri "$MailpitApi/messages" -Method Get).total
}

function Clear-Mailpit {
    try {
        Invoke-RestMethod -Uri "$MailpitApi/messages" -Method Delete | Out-Null
    } catch {
        Write-Warning "Impossible de vider Mailpit (ignoré)"
    }
}

function Publish-EmailEvent {
    param(
        [string]$RoutingKey,
        [string]$EventType,
        [hashtable]$Payload = @{}
    )
    $eventBody = @{
        eventId = [guid]::NewGuid().ToString()
        eventType = $EventType
        recipientEmail = $TestEmail
        recipientName = "Testeur"
        payload = $Payload
        correlationId = "mailpit-test-$([guid]::NewGuid().ToString('N'))"
    } | ConvertTo-Json -Compress

    $publishBody = @{
        properties = @{
            content_type = "application/json"
            delivery_mode = 2
            headers = @{ __TypeId__ = $TypeId }
        }
        routing_key = $RoutingKey
        payload = $eventBody
        payload_encoding = "string"
    } | ConvertTo-Json -Depth 6

    $uri = "$RabbitMgmt/exchanges/%2F/$Exchange/publish"
    $result = Invoke-RestMethod -Uri $uri -Method Post -Headers $AuthHeader -ContentType "application/json" -Body $publishBody
    if (-not $result.routed) {
        throw "Message non routé (routing_key=$RoutingKey)"
    }
    return $EventType
}

# Types couverts par email-service (RabbitMQ → Mailpit)
$notifications = @(
    @{ Key = "candidature.created";        Type = "CANDIDATURE_CREATED";        Payload = @{ offerTitle = "Offre test"; candidatureId = "c-1" } },
    @{ Key = "candidature.created";        Type = "CANDIDATURE.RECRUITER_NEW";   Payload = @{ offerTitle = "Offre test"; candidatureId = "c-1" } },
    @{ Key = "candidature.statut.changed"; Type = "CANDIDATURE_STATUT_CHANGED"; Payload = @{ offerTitle = "Offre test"; oldStatus = "SOUMISE"; newStatus = "EN_COURS"; comment = "" } },
    @{ Key = "candidature.statut.changed"; Type = "OFFER.CLOSED";               Payload = @{ offerTitle = "Offre test"; rejectedCount = 2 } },
    @{ Key = "entretien.planifie";         Type = "ENTRETIEN_PLANIFIE";         Payload = @{ offerTitle = "Offre test"; interviewDate = "30/05/2026 14:00"; interviewLocation = "Visio"; interviewerName = "RH" } },
    @{ Key = "entretien.planifie";         Type = "ENTRETIEN.ANNULATION";        Payload = @{ offerTitle = "Offre test"; comment = "Test annulation" } },
    @{ Key = "user.authentification.login";  Type = "AUTH.LOGIN";   Payload = @{ loginDateTime = "2026-05-29T10:00:00"; ipAddress = "127.0.0.1"; userAgent = "Test" } },
    @{ Key = "user.authentification.logout"; Type = "AUTH.LOGOUT";  Payload = @{ logoutDateTime = "2026-05-29T11:00:00" } },
    @{ Key = "user.authentification.register"; Type = "AUTH.REGISTER"; Payload = @{} },
    @{ Key = "user.authentification.register"; Type = "AUTH.OTP"; Payload = @{ otpCode = "123456"; otpValidityMinutes = 10 } },
    @{ Key = "recruiter.request.approved"; Type = "RECRUITER.APPROVED"; Payload = @{} },
    @{ Key = "recruiter.request.rejected"; Type = "RECRUITER.REJECTED"; Payload = @{ decisionMessage = "Test refus" } },
    @{ Key = "user.blocked"; Type = "ADMIN.USER_ACTION"; Payload = @{ action = "BLOCKED"; role = "CANDIDAT" } }
)

# Envoyés en SMTP direct par frontend-service (hors RabbitMQ) — test optionnel
$directSmtpTypes = @(
    "AUTH.OTP_INSCRIPTION (frontend SignupEmailVerificationService)",
    "AUTH.RESET_PASSWORD (frontend PasswordResetService)"
)

Write-Host "=== Test notifications Mailpit (RabbitMQ → email-service) ===" -ForegroundColor Cyan
Write-Host "Destinataire: $TestEmail"
Write-Host "Types SMTP direct (non testés ici): $($directSmtpTypes -join '; ')"

if (-not (Test-MailpitUp)) {
    Write-Error "Mailpit inaccessible — docker compose up -d mailpit"
    exit 1
}
Write-Host "Mailpit OK" -ForegroundColor Green

try {
    Invoke-RestMethod -Uri "$RabbitMgmt/overview" -Method Get -Headers $AuthHeader | Out-Null
} catch {
    Write-Error "RabbitMQ management inaccessible sur $RabbitMgmt"
    exit 1
}

$before = Get-MailCount
Clear-Mailpit
Start-Sleep -Seconds 1

$results = @()
foreach ($n in $notifications) {
    $type = $n.Type
    try {
        Publish-EmailEvent -RoutingKey $n.Key -EventType $type -Payload $n.Payload | Out-Null
        Start-Sleep -Milliseconds 1200
        $count = Get-MailCount
        $results += [pscustomobject]@{ Type = $type; Status = "OK (mailpit total=$count)" }
    } catch {
        $results += [pscustomobject]@{ Type = $type; Status = "KO ($($_.Exception.Message))" }
    }
}

$after = Get-MailCount
Write-Host ""
Write-Host "Messages Mailpit: $after (baseline $before)" -ForegroundColor Cyan
$results | Format-Table -AutoSize

$failed = @($results | Where-Object { $_.Status -notlike "OK*" })
if ($failed.Count -gt 0) {
    Write-Host "$($failed.Count) échec(s). Vérifiez email-service + MAIL_PORT=1025." -ForegroundColor Yellow
    exit 2
}
Write-Host "Tous les types RabbitMQ ont été publiés. Consultez http://localhost:8025" -ForegroundColor Green
exit 0
