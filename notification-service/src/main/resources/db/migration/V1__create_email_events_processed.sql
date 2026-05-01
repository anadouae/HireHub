
-- Create table for email events idempotence tracking
CREATE TABLE IF NOT EXISTS email_events_processed (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_event_id ON email_events_processed(event_id);
CREATE INDEX IF NOT EXISTS idx_recipient_email ON email_events_processed(recipient_email);
CREATE INDEX IF NOT EXISTS idx_processed_at ON email_events_processed(processed_at);

