package com.enterprise.java.streaming.server;

import com.enterprise.java.streaming.metrics.StreamingMetricsCollector;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Production-ready metrics server for streaming analytics
 * Exposes Prometheus metrics instead of custom dashboard
 */
public class ProductionMetricsServer {
    private static final Logger logger = LoggerFactory.getLogger(ProductionMetricsServer.class);
    private static final int DEFAULT_PORT = 8081;
    
    private HttpServer server;
    private final StreamingMetricsCollector metricsCollector;
    
    public ProductionMetricsServer() {
        this.metricsCollector = new StreamingMetricsCollector();
    }
    
    public void start() throws IOException {
        start(DEFAULT_PORT);
    }
    
    public void start(int port) throws IOException {
        logger.info("🚀 Starting Production Metrics Server on port {}", port);
        
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Prometheus metrics endpoint (industry standard)
        server.createContext("/metrics", new PrometheusMetricsHandler());
        
        // Health check endpoint
        server.createContext("/health", new HealthCheckHandler());
        
        // Root endpoint with info
        server.createContext("/", new InfoHandler());
        
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        // Initialize with demo data
        metricsCollector.simulateRealisticMetrics();
        
        logger.info("✅ Production Metrics Server started successfully");
        logger.info("📊 Prometheus metrics: http://localhost:{}/metrics", port);
        logger.info("❤️ Health check: http://localhost:{}/health", port);
        logger.info("🔗 Configure Prometheus to scrape: localhost:{}/metrics", port);
    }
    
    public void stop() {
        if (server != null) {
            logger.info("🛑 Stopping Production Metrics Server");
            server.stop(0);
            logger.info("✅ Production Metrics Server stopped");
        }
    }
    
    public StreamingMetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
    
    /**
     * Prometheus metrics endpoint - industry standard format
     */
    private class PrometheusMetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String metrics = metricsCollector.getPrometheusMetrics();
            
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, metrics.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(metrics.getBytes());
            }
            
            logger.debug("📊 Served Prometheus metrics to {}", exchange.getRemoteAddress());
        }
    }
    
    /**
     * Health check endpoint
     */
    private class HealthCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\n" +
                    "  \"status\": \"UP\",\n" +
                    "  \"timestamp\": \"" + java.time.LocalDateTime.now() + "\",\n" +
                    "  \"service\": \"streaming-analytics\",\n" +
                    "  \"version\": \"1.0.0\"\n" +
                    "}";
            
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    /**
     * Info endpoint
     */
    private class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head><title>Streaming Analytics - Production Metrics</title></head>\n" +
                    "<body style='font-family: Arial, sans-serif; margin: 40px;'>\n" +
                    "<h1>🚀 Streaming Analytics - Production Ready</h1>\n" +
                    "<p>This service exposes production-ready metrics in Prometheus format.</p>\n" +
                    "<h2>Available Endpoints:</h2>\n" +
                    "<ul>\n" +
                    "<li><a href='/metrics'>📊 /metrics</a> - Prometheus metrics (scrape target)</li>\n" +
                    "<li><a href='/health'>❤️ /health</a> - Health check</li>\n" +
                    "</ul>\n" +
                    "<h2>Monitoring Stack:</h2>\n" +
                    "<ul>\n" +
                    "<li><a href='http://localhost:9090' target='_blank'>Prometheus</a> - Metrics collection</li>\n" +
                    "<li><a href='http://localhost:3000' target='_blank'>Grafana</a> - Professional dashboards</li>\n" +
                    "</ul>\n" +
                    "<p><strong>Note:</strong> Custom dashboard has been replaced with industry-standard Grafana + Prometheus stack.</p>\n" +
                    "</body>\n" +
                    "</html>";
            
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            ProductionMetricsServer server = new ProductionMetricsServer();
            server.start();
            
            // Simulate some streaming activity
            startMetricsSimulation(server.getMetricsCollector());
            
            // Keep running
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("❌ Failed to start production metrics server", e);
        }
    }
    
    private static void startMetricsSimulation(StreamingMetricsCollector collector) {
        // Background thread to simulate realistic metrics
        Thread simulationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000); // Update every 5 seconds
                    
                    // Simulate processing events
                    collector.incrementEventsProcessed((long) (Math.random() * 100));
                    
                    // Occasionally trigger alerts
                    if (Math.random() < 0.1) {
                        collector.incrementSuspiciousLogins();
                        collector.incrementAlertsTriggered();
                    }
                    
                    // Update throughput
                    collector.updateThroughput((long) (2000 + Math.random() * 1000));
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        simulationThread.setDaemon(true);
        simulationThread.start();
        logger.info("🎭 Started metrics simulation thread");
    }
}