-- event_id peut contenir un préfixe (ex. verify-uuid) ou un UUID seul
ALTER TABLE event_audit_log ALTER COLUMN event_id TYPE VARCHAR(255);
