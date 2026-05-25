CREATE TABLE IF NOT EXISTS notification_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    channel VARCHAR(32) NOT NULL,
    template VARCHAR(64) NOT NULL,
    payload_json TEXT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_user ON notification_logs(user_id);
