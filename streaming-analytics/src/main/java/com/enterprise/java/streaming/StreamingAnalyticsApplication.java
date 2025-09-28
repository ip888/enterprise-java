package com.enterprise.java.streaming;

import com.enterprise.java.streaming.flink.FlinkStreamProcessor;
import com.enterprise.java.streaming.spark.SparkStreamAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class for the Streaming Analytics Pipeline.
 * 
 * This application demonstrates:
 * - Apache Spark streaming for ML model training and batch analytics
 * - Apache Flink streaming for real-time complex event processing
 * - Integration with Kafka streams from Project 1 (User Service)
 * - Real-time analytics dashboard
 * 
 * Architecture:
 * 1. Consumes events from Project 1's Kafka topics (user-events, notifications)
 * 2. Uses Flink for real-time stream processing and alerting
 * 3. Uses Spark for ML model training and batch analytics
 * 4. Stores insights in PostgreSQL for dashboard consumption
 */
public class StreamingAnalyticsApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamingAnalyticsApplication.class);
    
    public static void main(String[] args) {
        logger.info("🚀 Starting Enterprise Streaming Analytics Pipeline");
        logger.info("📊 Integrating Apache Spark & Flink for real-time insights");
        
        try {
            // Create executor service for parallel processing
            ExecutorService executor = Executors.newFixedThreadPool(3);
            
            // Initialize and start Spark streaming application
            SparkStreamAnalyzer sparkAnalyzer = new SparkStreamAnalyzer();
            executor.submit(() -> {
                try {
                    logger.info("🔥 Starting Apache Spark streaming analytics...");
                    sparkAnalyzer.startStreaming();
                } catch (Exception e) {
                    logger.error("❌ Error in Spark streaming application", e);
                }
            });
            
            // Initialize and start Flink streaming application
            FlinkStreamProcessor flinkProcessor = new FlinkStreamProcessor();
            executor.submit(() -> {
                try {
                    logger.info("⚡ Starting Apache Flink stream processing...");
                    flinkProcessor.startProcessing();
                } catch (Exception e) {
                    logger.error("❌ Error in Flink streaming application", e);
                }
            });
            
            // Start monitoring dashboard
            executor.submit(() -> {
                try {
                    logger.info("📈 Starting Analytics Dashboard...");
                    AnalyticsDashboard dashboard = new AnalyticsDashboard();
                    dashboard.startDashboard();
                } catch (Exception e) {
                    logger.error("❌ Error in Analytics Dashboard", e);
                }
            });
            
            // Add shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("🛑 Shutting down Streaming Analytics Pipeline...");
                try {
                    sparkAnalyzer.stop();
                    flinkProcessor.stop();
                    executor.shutdown();
                    logger.info("✅ Graceful shutdown completed");
                } catch (Exception e) {
                    logger.error("❌ Error during shutdown", e);
                }
            }));
            
            logger.info("✅ Enterprise Streaming Analytics Pipeline started successfully");
            logger.info("🌐 Dashboard available at: http://localhost:8082");
            logger.info("📊 Spark UI available at: http://localhost:4040");
            logger.info("⚡ Flink UI available at: http://localhost:8081");
            
            // Keep main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("❌ Failed to start Streaming Analytics Pipeline", e);
            System.exit(1);
        }
    }
}