package com.enterprise.portfolio.userservice.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<String>> getDashboard() {
        return Mono.fromCallable(() -> {
            try {
                // Read the HTML file from the project directory
                String htmlContent = Files.readString(
                    Paths.get("monitoring-dashboard.html"), 
                    StandardCharsets.UTF_8
                );
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);
            } catch (IOException e) {
                // If file not found, return embedded HTML
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(getEmbeddedDashboardHtml());
            }
        });
    }
    
    @GetMapping(value = "/embedded", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<String>> getEmbeddedDashboard() {
        return Mono.just(ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(getEmbeddedDashboardHtml()));
    }

    private String getEmbeddedDashboardHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Enterprise Java - Kafka & Redis Monitoring Dashboard</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            padding: 20px;
        }

        .dashboard {
            max-width: 1400px;
            margin: 0 auto;
        }

        .header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }

        .header h1 {
            font-size: 2.5rem;
            margin-bottom: 10px;
        }

        .header p {
            font-size: 1.2rem;
            opacity: 0.9;
        }

        .controls {
            display: flex;
            justify-content: center;
            gap: 15px;
            margin-bottom: 30px;
            flex-wrap: wrap;
        }

        .btn {
            background: #4CAF50;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        }

        .btn.redis { background: #dc3545; }
        .btn.kafka { background: #ff6b35; }
        .btn.full { background: #6f42c1; }
        .btn.reset { background: #6c757d; }

        .status-bar {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .status-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            text-align: center;
            transition: transform 0.3s ease;
        }

        .status-card:hover {
            transform: translateY(-3px);
        }

        .status-card.up {
            border-left: 5px solid #28a745;
        }

        .status-card.down {
            border-left: 5px solid #dc3545;
        }

        .status-value {
            font-size: 2rem;
            font-weight: bold;
            margin: 10px 0;
        }

        .status-up { color: #28a745; }
        .status-down { color: #dc3545; }

        .metrics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .metric-card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        .metric-card.user-metrics {
            background: linear-gradient(135deg, #f8f9ff 0%, #e8f0ff 100%);
            border: 2px solid #007bff;
            border-left: 5px solid #007bff;
        }

        .metric-card.user-metrics .metric-title {
            color: #0056b3;
        }

        .metric-card.user-metrics .metric-value {
            color: #0056b3;
        }

        .metric-title {
            font-size: 1.3rem;
            font-weight: bold;
            margin-bottom: 15px;
            color: #333;
        }

        .metric-value {
            font-size: 2.5rem;
            font-weight: bold;
            color: #007bff;
            margin-bottom: 10px;
        }

        .metric-description {
            color: #666;
            font-size: 0.9rem;
        }

        .activity-log {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            height: 300px;
            overflow-y: auto;
        }

        .log-entry {
            padding: 8px 12px;
            margin-bottom: 8px;
            border-radius: 6px;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
        }

        .log-info { background: #e3f2fd; color: #1976d2; }
        .log-success { background: #e8f5e8; color: #2e7d32; }
        .log-error { background: #ffebee; color: #c62828; }

        .loading {
            text-align: center;
            color: #666;
            font-style: italic;
            padding: 20px;
        }

        .timestamp {
            font-size: 0.8rem;
            color: #999;
            float: right;
        }

        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.05); }
            100% { transform: scale(1); }
        }

        .live-indicator {
            animation: pulse 2s infinite;
            color: #28a745;
            font-weight: bold;
        }

        .connection-details {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-top: 30px;
        }

        .detail-card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        .detail-title {
            font-size: 1.4rem;
            font-weight: bold;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .detail-title.redis { color: #dc3545; }
        .detail-title.kafka { color: #ff6b35; }

        .detail-item {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #eee;
        }

        .detail-item:last-child {
            border-bottom: none;
        }

        .java-powered {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            margin-top: 20px;
            text-align: center;
        }

        .java-powered h3 {
            color: #f89820;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="header">
            <h1>☕ Enterprise Java</h1>
            <p>Real-Time User Operations & Infrastructure Monitoring</p>
            <p class="live-indicator">● LIVE TRACKING - USER API OPERATIONS REFLECTED IN REAL-TIME</p>
        </div>

        <div class="java-powered">
            <h3>☕ 100% Java Implementation</h3>
            <p>This dashboard is served by Spring Boot WebFlux</p>
        </div>

        <div class="controls">
            <button class="btn redis" onclick="simulateRedisActivity()">🔴 Test Redis Caching</button>
            <button class="btn kafka" onclick="simulateKafkaActivity()">🟠 Test Kafka Events</button>
            <button class="btn full" onclick="simulateFullActivity()">🟣 Full Integration Test</button>
            <button class="btn reset" onclick="resetMetrics()">🔄 Reset Counters</button>
        </div>

        <div class="status-bar">
            <div class="status-card" id="redis-status">
                <h3>Redis Cache</h3>
                <div class="status-value" id="redis-value">...</div>
                <div>Connection Status</div>
            </div>
            <div class="status-card" id="kafka-status">
                <h3>Kafka Messaging</h3>
                <div class="status-value" id="kafka-value">...</div>
                <div>Connection Status</div>
            </div>
            <div class="status-card">
                <h3>Overall Health</h3>
                <div class="status-value" id="health-value">...</div>
                <div>System Status</div>
            </div>
            <div class="status-card">
                <h3>Last Updated</h3>
                <div class="status-value" id="last-update">...</div>
                <div>Timestamp</div>
            </div>
        </div>

        <div class="metrics-grid">
            <!-- User Operations Metrics -->
            <div class="metric-card user-metrics">
                <div class="metric-title">👤 User Registrations</div>
                <div class="metric-value" id="user-registrations">0</div>
                <div class="metric-description">Total users registered</div>
            </div>
            <div class="metric-card user-metrics">
                <div class="metric-title">🔐 User Logins</div>
                <div class="metric-value" id="user-logins">0</div>
                <div class="metric-description">Authentication events</div>
            </div>
            <div class="metric-card user-metrics">
                <div class="metric-title">✏️ Profile Updates</div>
                <div class="metric-value" id="user-updates">0</div>
                <div class="metric-description">User profile modifications</div>
            </div>
            <div class="metric-card user-metrics">
                <div class="metric-title">🔍 User Searches</div>
                <div class="metric-value" id="user-searches">0</div>
                <div class="metric-description">Search operations performed</div>
            </div>
            <div class="metric-card user-metrics">
                <div class="metric-title">📊 Total User Ops</div>
                <div class="metric-value" id="total-user-ops">0</div>
                <div class="metric-description">Total user operations</div>
            </div>
            
            <!-- Infrastructure Metrics -->
            <div class="metric-card">
                <div class="metric-title">🔴 Redis Operations</div>
                <div class="metric-value" id="redis-ops">0</div>
                <div class="metric-description">Cache operations performed</div>
            </div>
            <div class="metric-card">
                <div class="metric-title">📡 Kafka Events</div>
                <div class="metric-value" id="kafka-events">0</div>
                <div class="metric-description">Events published to Kafka</div>
            </div>
            <div class="metric-card">
                <div class="metric-title">📈 Cache Hit Ratio</div>
                <div class="metric-value" id="cache-ratio">0%</div>
                <div class="metric-description">Cache performance percentage</div>
            </div>
            <div class="metric-card">
                <div class="metric-title">✅ Cache Hits</div>
                <div class="metric-value" id="cache-hits">0</div>
                <div class="metric-description">Successful cache retrievals</div>
            </div>
            <div class="metric-card">
                <div class="metric-title">❌ Cache Misses</div>
                <div class="metric-value" id="cache-misses">0</div>
                <div class="metric-description">Cache lookup failures</div>
            </div>
            <div class="metric-card">
                <div class="metric-title">🔑 Active Keys</div>
                <div class="metric-value" id="active-keys">0</div>
                <div class="metric-description">Current keys stored in Redis</div>
            </div>
        </div>

        <div class="connection-details">
            <div class="detail-card">
                <div class="detail-title redis">🔴 Redis Connection Details</div>
                <div id="redis-details">
                    <div class="loading">Loading Redis details...</div>
                </div>
            </div>
            <div class="detail-card">
                <div class="detail-title kafka">🟠 Kafka Connection Details</div>
                <div id="kafka-details">
                    <div class="loading">Loading Kafka details...</div>
                </div>
            </div>
        </div>

        <div class="metric-card">
            <div class="metric-title">📊 Activity Log - Real-time Operations</div>
            <div class="activity-log" id="activity-log">
                <div class="log-entry log-info">Java-based monitoring dashboard initialized...</div>
            </div>
        </div>
    </div>

    <script>
        const API_BASE = 'http://localhost:8081/api/v1';
        let logCounter = 1;

        // Initialize dashboard
        document.addEventListener('DOMContentLoaded', function() {
            loadDashboardData();
            startLiveUpdates();
            addLogEntry('Java monitoring dashboard started - connecting to enterprise services...', 'info');
        });

        // Load dashboard data
        async function loadDashboardData() {
            try {
                const response = await fetch(`${API_BASE}/monitoring/dashboard`);
                const data = await response.json();
                updateDashboard(data);
                addLogEntry('Dashboard data updated successfully via Java REST API', 'success');
            } catch (error) {
                addLogEntry(`Failed to load dashboard data: ${error.message}`, 'error');
                console.error('Error loading dashboard:', error);
            }
        }

        // Update dashboard with new data
        function updateDashboard(data) {
            // Update status indicators
            updateStatus('redis-status', 'redis-value', data.redis_status);
            updateStatus('kafka-status', 'kafka-value', data.kafka_status);
            
            // Update health
            const healthStatus = (data.redis_status === 'UP' && data.kafka_status === 'UP') ? 'HEALTHY' : 'DEGRADED';
            document.getElementById('health-value').textContent = healthStatus;
            document.getElementById('health-value').className = 'status-value ' + (healthStatus === 'HEALTHY' ? 'status-up' : 'status-down');
            
            // Update timestamp
            const timestamp = new Date(data.timestamp).toLocaleTimeString();
            document.getElementById('last-update').textContent = timestamp;
            
            // Update metrics - prioritize real_time_metrics over legacy metrics
            const metrics = data.real_time_metrics || data.legacy_metrics || {};
            
            // Display actual user operation metrics
            if (metrics.user_registrations !== undefined) {
                document.getElementById('user-registrations').textContent = metrics.user_registrations || 0;
            }
            if (metrics.user_logins !== undefined) {
                document.getElementById('user-logins').textContent = metrics.user_logins || 0;
            }
            if (metrics.user_updates !== undefined) {
                document.getElementById('user-updates').textContent = metrics.user_updates || 0;
            }
            if (metrics.user_searches !== undefined) {
                document.getElementById('user-searches').textContent = metrics.user_searches || 0;
            }
            if (metrics.total_user_operations !== undefined) {
                document.getElementById('total-user-ops').textContent = metrics.total_user_operations || 0;
            }
            
            // Infrastructure metrics
            document.getElementById('redis-ops').textContent = metrics.redis_operations || 0;
            document.getElementById('kafka-events').textContent = metrics.kafka_events || 0;
            document.getElementById('cache-ratio').textContent = (metrics.cache_hit_ratio || 0).toFixed(1) + '%';
            document.getElementById('cache-hits').textContent = metrics.cache_hits || 0;
            document.getElementById('cache-misses').textContent = metrics.cache_misses || 0;
            
            // Update Redis details
            if (data.redis_stats) {
                updateRedisDetails(data.redis_stats);
                document.getElementById('active-keys').textContent = data.redis_stats.total_keys || 0;
            }
            
            // Update Kafka details
            if (data.kafka_stats) {
                updateKafkaDetails(data.kafka_stats);
            }
        }

        // Update status card
        function updateStatus(cardId, valueId, status) {
            const card = document.getElementById(cardId);
            const value = document.getElementById(valueId);
            
            value.textContent = status;
            card.className = 'status-card ' + (status === 'UP' ? 'up' : 'down');
            value.className = 'status-value ' + (status === 'UP' ? 'status-up' : 'status-down');
        }

        // Update Redis details
        function updateRedisDetails(stats) {
            const container = document.getElementById('redis-details');
            container.innerHTML = '';
            
            const details = [
                ['Status', stats.connection_status || 'Unknown'],
                ['Total Keys', stats.total_keys || 0],
                ['Last Check', new Date(stats.last_check).toLocaleString()]
            ];
            
            details.forEach(([key, value]) => {
                const item = document.createElement('div');
                item.className = 'detail-item';
                item.innerHTML = `<span><strong>${key}:</strong></span><span>${value}</span>`;
                container.appendChild(item);
            });
        }

        // Update Kafka details
        function updateKafkaDetails(stats) {
            const container = document.getElementById('kafka-details');
            container.innerHTML = '';
            
            const details = [
                ['Status', stats.connection_status || 'Unknown'],
                ['Bootstrap Servers', stats.bootstrap_servers || 'localhost:9092'],
                ['Last Check', new Date(stats.last_check).toLocaleString()]
            ];
            
            details.forEach(([key, value]) => {
                const item = document.createElement('div');
                item.className = 'detail-item';
                item.innerHTML = `<span><strong>${key}:</strong></span><span>${value}</span>`;
                container.appendChild(item);
            });
        }

        // Start live updates
        function startLiveUpdates() {
            setInterval(() => {
                loadDashboardData();
                loadActivityLog(); // Load real activity log showing user operations
            }, 3000); // Update every 3 seconds
        }

        // Simulate Redis activity
        async function simulateRedisActivity() {
            addLogEntry('🔴 Triggering Redis cache operations via Java service...', 'info');
            try {
                const response = await fetch(`${API_BASE}/monitoring/simulate/redis-activity`, {
                    method: 'POST'
                });
                const result = await response.json();
                addLogEntry(`✅ Redis test completed: ${result.operations_executed} cache operations executed`, 'success');
                loadDashboardData(); // Refresh dashboard
            } catch (error) {
                addLogEntry(`❌ Redis test failed: ${error.message}`, 'error');
            }
        }

        // Simulate Kafka activity
        async function simulateKafkaActivity() {
            addLogEntry('🟠 Publishing Kafka events via Java producer...', 'info');
            try {
                const response = await fetch(`${API_BASE}/monitoring/simulate/kafka-activity`, {
                    method: 'POST'
                });
                const result = await response.json();
                addLogEntry(`✅ Kafka test completed: ${result.events_published} events published to topics`, 'success');
                loadDashboardData(); // Refresh dashboard
            } catch (error) {
                addLogEntry(`❌ Kafka test failed: ${error.message}`, 'error');
            }
        }

        // Simulate full activity
        async function simulateFullActivity() {
            addLogEntry('🟣 Starting comprehensive integration test...', 'info');
            try {
                const response = await fetch(`${API_BASE}/monitoring/simulate/full-activity`, {
                    method: 'POST'
                });
                const result = await response.json();
                addLogEntry('✅ Full system integration test completed - Redis & Kafka working together', 'success');
                loadDashboardData(); // Refresh dashboard
            } catch (error) {
                addLogEntry(`❌ Integration test failed: ${error.message}`, 'error');
            }
        }

        // Reset metrics
        async function resetMetrics() {
            addLogEntry('🔄 Resetting all monitoring counters...', 'info');
            try {
                const response = await fetch(`${API_BASE}/monitoring/reset-metrics`, {
                    method: 'DELETE'
                });
                const result = await response.json();
                addLogEntry('✅ All metrics reset to zero', 'success');
                loadDashboardData(); // Refresh dashboard
            } catch (error) {
                addLogEntry(`❌ Metric reset failed: ${error.message}`, 'error');
            }
        }

        // Load activity log
        async function loadActivityLog() {
            try {
                const response = await fetch(`${API_BASE}/monitoring/activity-log`);
                const data = await response.json();
                
                if (data.recent_activity && Array.isArray(data.recent_activity)) {
                    // Clear existing log entries
                    const log = document.getElementById('activity-log');
                    log.innerHTML = '';
                    
                    // Add each activity to the log
                    data.recent_activity.forEach(activity => {
                        const entry = document.createElement('div');
                        entry.className = `log-entry log-${activity.level}`;
                        
                        const activityTime = new Date(activity.timestamp).toLocaleTimeString();
                        entry.innerHTML = `
                            <span>[${activity.type}] ${activity.message}</span>
                            <span class="timestamp">${activityTime}</span>
                        `;
                        
                        log.appendChild(entry);
                    });
                    
                    addLogEntry('Activity log updated with real user operations', 'success');
                }
            } catch (error) {
                addLogEntry(`Failed to load activity log: ${error.message}`, 'error');
                console.error('Error loading activity log:', error);
            }
        }

        // Add log entry
        function addLogEntry(message, type) {
            const log = document.getElementById('activity-log');
            const entry = document.createElement('div');
            entry.className = `log-entry log-${type}`;
            
            const timestamp = new Date().toLocaleTimeString();
            entry.innerHTML = `
                <span>${String(logCounter++).padStart(3, '0')}. ${message}</span>
                <span class="timestamp">${timestamp}</span>
            `;
            
            log.insertBefore(entry, log.firstChild);
            
            // Keep only last 50 log entries
            while (log.children.length > 50) {
                log.removeChild(log.lastChild);
            }
        }
    </script>
</body>
</html>
""";
    }
}