-- Initialize analytics database schema

-- Create analytics results table
CREATE TABLE IF NOT EXISTS analytics_results (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_count INTEGER NOT NULL,
    unique_sessions INTEGER NOT NULL,
    analysis_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    window_start TIMESTAMP,
    window_end TIMESTAMP,
    avg_events_per_session DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create alerts table
CREATE TABLE IF NOT EXISTS security_alerts (
    id SERIAL PRIMARY KEY,
    alert_id VARCHAR(100) UNIQUE NOT NULL,
    user_id VARCHAR(100),
    alert_type VARCHAR(50) NOT NULL,
    description TEXT,
    severity VARCHAR(20) NOT NULL,
    event_count INTEGER,
    resolved BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user behavior patterns table
CREATE TABLE IF NOT EXISTS user_behavior_patterns (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    pattern_id VARCHAR(100) UNIQUE NOT NULL,
    pattern_type VARCHAR(50) NOT NULL,
    confidence DECIMAL(5,3),
    user_segment VARCHAR(50),
    risk_score DECIMAL(5,3),
    features JSONB,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create ML model metrics table
CREATE TABLE IF NOT EXISTS ml_model_metrics (
    id SERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(20) NOT NULL,
    accuracy DECIMAL(5,3),
    precision_score DECIMAL(5,3),
    recall_score DECIMAL(5,3),
    f1_score DECIMAL(5,3),
    training_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create streaming metrics table
CREATE TABLE IF NOT EXISTS streaming_metrics (
    id SERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value BIGINT NOT NULL,
    component VARCHAR(50) NOT NULL, -- 'spark', 'flink', 'kafka'
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_analytics_user_id ON analytics_results(user_id);
CREATE INDEX IF NOT EXISTS idx_analytics_timestamp ON analytics_results(analysis_timestamp);
CREATE INDEX IF NOT EXISTS idx_alerts_user_id ON security_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_alerts_timestamp ON security_alerts(timestamp);
CREATE INDEX IF NOT EXISTS idx_alerts_severity ON security_alerts(severity);
CREATE INDEX IF NOT EXISTS idx_patterns_user_id ON user_behavior_patterns(user_id);
CREATE INDEX IF NOT EXISTS idx_patterns_timestamp ON user_behavior_patterns(timestamp);
CREATE INDEX IF NOT EXISTS idx_metrics_component ON streaming_metrics(component);
CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON streaming_metrics(timestamp);

-- Insert some sample data for testing
INSERT INTO ml_model_metrics (model_name, model_version, accuracy, precision_score, recall_score, f1_score, training_timestamp) VALUES
('user-segmentation-kmeans', '1.0', 0.947, 0.923, 0.951, 0.937, CURRENT_TIMESTAMP),
('anomaly-detection-isolation-forest', '1.1', 0.889, 0.876, 0.902, 0.889, CURRENT_TIMESTAMP),
('behavior-classification-random-forest', '2.0', 0.912, 0.898, 0.925, 0.911, CURRENT_TIMESTAMP);

-- Create a view for real-time dashboard queries
CREATE OR REPLACE VIEW dashboard_summary AS
SELECT 
    COUNT(*) as total_analytics_records,
    COUNT(DISTINCT user_id) as unique_users_analyzed,
    (SELECT COUNT(*) FROM security_alerts WHERE created_at >= CURRENT_DATE) as alerts_today,
    (SELECT COUNT(*) FROM security_alerts WHERE severity IN ('HIGH', 'CRITICAL') AND resolved = false) as active_high_priority_alerts,
    (SELECT AVG(accuracy) FROM ml_model_metrics WHERE training_timestamp >= CURRENT_TIMESTAMP - INTERVAL '1 day') as avg_model_accuracy
FROM analytics_results 
WHERE created_at >= CURRENT_DATE;

COMMIT;