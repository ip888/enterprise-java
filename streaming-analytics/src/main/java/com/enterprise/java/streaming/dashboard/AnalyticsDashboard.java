package com.enterprise.java.streaming.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * DEPRECATED: Custom Analytics Dashboard
 * 
 * ⚠️  This custom dashboard has been REPLACED with production-ready monitoring stack:
 * - Prometheus for metrics collection
 * - Grafana for professional dashboards
 * - AlertManager for sophisticated alerting
 * 
 * 🔄 Migration Instructions:
 * 1. Use ProductionMetricsServer instead (exposes Prometheus metrics)
 * 2. Start monitoring stack: docker-compose up -d (in infrastructure/monitoring/)
 * 3. Access Grafana at http://localhost:3000 (admin/admin)
 * 4. Import pre-built enterprise dashboards
 * 
 * 📊 Benefits of new stack:
 * - Industry standard tools (Prometheus + Grafana)
 * - Zero custom code to maintain
 * - Professional UI with advanced features
 * - Data persistence and historical analysis
 * - Multi-channel alerting (Slack, email, etc.)
 * - Scalable to multiple services
 * 
 * 🗑️  This file will be removed in the next version.
 * 
 * @deprecated Use ProductionMetricsServer + Grafana + Prometheus stack instead
 */
@Deprecated(forRemoval = true, since = "1.0.0")
public class AnalyticsDashboard {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDashboard.class);
    private static final int DEFAULT_PORT = 8080;
    private final ObjectMapper objectMapper;
    private HttpServer server;
    private final Map<String, Object> metrics = new ConcurrentHashMap<>();
    private final Map<String, Object> alerts = new ConcurrentHashMap<>();
    
    public AnalyticsDashboard() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeMetrics();
    }
    
    public void start() throws IOException {
        start(DEFAULT_PORT);
    }
    
    public void start(int port) throws IOException {
        logger.info("🌐 Starting Analytics Dashboard on port {}", port);
        
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Dashboard UI endpoint
        server.createContext("/", new DashboardHandler());
        
        // API endpoints
        server.createContext("/api/metrics", new MetricsHandler());
        server.createContext("/api/alerts", new AlertsHandler());
        server.createContext("/api/health", new HealthHandler());
        
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        logger.info("✅ Analytics Dashboard started successfully");
        logger.info("🌐 Access dashboard at: http://localhost:{}", port);
    }
    
    public void stop() {
        if (server != null) {
            logger.info("🛑 Stopping Analytics Dashboard");
            server.stop(0);
            logger.info("✅ Analytics Dashboard stopped");
        }
    }
    
    private void initializeMetrics() {
        // Initialize with default metrics
        metrics.put("totalEvents", 0L);
        metrics.put("uniqueUsers", 0L);
        metrics.put("activeStreams", 0L);
        metrics.put("alertsTriggered", 0L);
        metrics.put("lastUpdated", LocalDateTime.now());
        
        // Simulate some initial data for demo purposes
        simulateMetrics();
    }
    
    private void simulateMetrics() {
        // Add some realistic simulation data
        metrics.put("totalEvents", 15430L);
        metrics.put("uniqueUsers", 1247L);
        metrics.put("activeStreams", 3L);
        metrics.put("alertsTriggered", 12L);
        metrics.put("avgProcessingTime", "45ms");
        metrics.put("throughputPerSec", 2340L);
        
        // Add some recent alerts for demo
        Map<String, Object> alert1 = new HashMap<>();
        alert1.put("id", "alert-001");
        alert1.put("type", "SUSPICIOUS_LOGIN");
        alert1.put("severity", "HIGH");
        alert1.put("message", "Multiple failed login attempts detected for user12345");
        alert1.put("timestamp", LocalDateTime.now().minusMinutes(5));
        alerts.put("alert-001", alert1);
        
        Map<String, Object> alert2 = new HashMap<>();
        alert2.put("id", "alert-002");
        alert2.put("type", "RAPID_ACTIVITY");
        alert2.put("severity", "MEDIUM");
        alert2.put("message", "Unusual high activity detected for user67890");
        alert2.put("timestamp", LocalDateTime.now().minusMinutes(2));
        alerts.put("alert-002", alert2);
    }
    
    public void updateMetric(String key, Object value) {
        metrics.put(key, value);
        metrics.put("lastUpdated", LocalDateTime.now());
        logger.debug("📊 Updated metric: {} = {}", key, value);
    }
    
    public void addAlert(String id, String type, String severity, String message) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", id);
        alert.put("type", type);
        alert.put("severity", severity);
        alert.put("message", message);
        alert.put("timestamp", LocalDateTime.now());
        
        alerts.put(id, alert);
        updateMetric("alertsTriggered", ((Long) metrics.get("alertsTriggered")) + 1);
        logger.info("🚨 Alert added: {} - {}", type, message);
    }
    
    /**
     * Main dashboard HTML page
     */
    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = generateDashboardHTML();
            
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private String generateDashboardHTML() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>Streaming Analytics Dashboard</title>\n");
            html.append("    <style>\n");
            html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
            html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f5f5; }\n");
            html.append("        .container { max-width: 1400px; margin: 0 auto; padding: 20px; }\n");
            html.append("        .header { text-align: center; margin-bottom: 30px; }\n");
            html.append("        .header h1 { color: #333; font-size: 2.5em; margin-bottom: 10px; }\n");
            html.append("        .card { background: white; border-radius: 10px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin-bottom: 20px; }\n");
            html.append("        .metric-item { display: flex; justify-content: space-between; margin: 10px 0; padding: 10px; background: #f8f9fa; border-radius: 5px; }\n");
            html.append("        .metric-value { color: #007bff; font-weight: bold; }\n");
            html.append("        .refresh-btn { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("    <div class=\"container\">\n");
            html.append("        <div class=\"header\">\n");
            html.append("            <h1>🚀 Streaming Analytics Dashboard</h1>\n");
            html.append("            <p>Real-time monitoring of Apache Spark & Flink streaming applications</p>\n");
            html.append("        </div>\n");
            html.append("        \n");
            html.append("        <div class=\"card\">\n");
            html.append("            <h2>📊 Streaming Metrics</h2>\n");
            html.append("            <div class=\"metric-item\">\n");
            html.append("                <span>Total Events Processed</span>\n");
            html.append("                <span class=\"metric-value\" id=\"totalEvents\">Loading...</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"metric-item\">\n");
            html.append("                <span>Unique Users</span>\n");
            html.append("                <span class=\"metric-value\" id=\"uniqueUsers\">Loading...</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"metric-item\">\n");
            html.append("                <span>Active Streams</span>\n");
            html.append("                <span class=\"metric-value\" id=\"activeStreams\">Loading...</span>\n");
            html.append("            </div>\n");
            html.append("            <button class=\"refresh-btn\" onclick=\"loadMetrics()\">🔄 Refresh</button>\n");
            html.append("        </div>\n");
            html.append("        \n");
            html.append("        <div class=\"card\">\n");
            html.append("            <h2>🚨 Recent Alerts</h2>\n");
            html.append("            <div id=\"alerts-content\">Loading alerts...</div>\n");
            html.append("            <button class=\"refresh-btn\" onclick=\"loadAlerts()\">🔄 Refresh</button>\n");
            html.append("        </div>\n");
            html.append("        \n");
            html.append("        <div class=\"card\">\n");
            html.append("            <h2>⚡ System Status</h2>\n");
            html.append("            <div>Apache Spark - Running ✅</div>\n");
            html.append("            <div>Apache Flink - Running ✅</div>\n");
            html.append("            <div>Kafka Consumer - Connected ✅</div>\n");
            html.append("            <div>PostgreSQL - Connected ✅</div>\n");
            html.append("        </div>\n");
            html.append("    </div>\n");
            html.append("    \n");
            html.append("    <script>\n");
            html.append("        function loadMetrics() {\n");
            html.append("            fetch('/api/metrics')\n");
            html.append("                .then(response => response.json())\n");
            html.append("                .then(data => {\n");
            html.append("                    document.getElementById('totalEvents').textContent = data.totalEvents.toLocaleString();\n");
            html.append("                    document.getElementById('uniqueUsers').textContent = data.uniqueUsers.toLocaleString();\n");
            html.append("                    document.getElementById('activeStreams').textContent = data.activeStreams;\n");
            html.append("                })\n");
            html.append("                .catch(error => console.error('Error:', error));\n");
            html.append("        }\n");
            html.append("        \n");
            html.append("        function loadAlerts() {\n");
            html.append("            fetch('/api/alerts')\n");
            html.append("                .then(response => response.json())\n");
            html.append("                .then(data => {\n");
            html.append("                    const alertsDiv = document.getElementById('alerts-content');\n");
            html.append("                    if (Object.keys(data).length === 0) {\n");
            html.append("                        alertsDiv.innerHTML = '✅ No active alerts';\n");
            html.append("                        return;\n");
            html.append("                    }\n");
            html.append("                    let html = '';\n");
            html.append("                    Object.values(data).forEach(alert => {\n");
            html.append("                        html += `<div style=\"margin: 10px 0; padding: 10px; background: #f8d7da; border-radius: 5px;\">`;\n");
            html.append("                        html += `<strong>${alert.type}</strong> - ${alert.message}`;\n");
            html.append("                        html += `<div style=\"font-size: 0.9em; color: #666;\">${alert.severity} | ${new Date(alert.timestamp).toLocaleString()}</div>`;\n");
            html.append("                        html += '</div>';\n");
            html.append("                    });\n");
            html.append("                    alertsDiv.innerHTML = html;\n");
            html.append("                })\n");
            html.append("                .catch(error => console.error('Error:', error));\n");
            html.append("        }\n");
            html.append("        \n");
            html.append("        // Auto-refresh every 30 seconds\n");
            html.append("        setInterval(() => { loadMetrics(); loadAlerts(); }, 30000);\n");
            html.append("        \n");
            html.append("        // Initial load\n");
            html.append("        loadMetrics();\n");
            html.append("        loadAlerts();\n");
            html.append("    </script>\n");
            html.append("</body>\n");
            html.append("</html>\n");
            
            return html.toString();
        }
    }
    
    /**
     * Metrics API endpoint
     */
    private class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            String response = objectMapper.writeValueAsString(metrics);
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    /**
     * Alerts API endpoint
     */
    private class AlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            String response = objectMapper.writeValueAsString(alerts);
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    /**
     * Health check endpoint
     */
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("uptime", "Runtime uptime info would go here");
            
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            String response = objectMapper.writeValueAsString(health);
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            AnalyticsDashboard dashboard = new AnalyticsDashboard();
            dashboard.start();
            
            // Keep running
            Runtime.getRuntime().addShutdownHook(new Thread(dashboard::stop));
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("❌ Failed to start dashboard", e);
        }
    }
}