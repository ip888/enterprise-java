package com.enterprise.java.streaming;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic unit tests for the Streaming Analytics application.
 * These tests validate core functionality without requiring external dependencies.
 */
class StreamingAnalyticsApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(StreamingAnalyticsApplicationTests.class);

    @Test
    void applicationMainClassExists() {
        // Verify that the main application class can be loaded
        try {
            Class.forName("com.enterprise.java.streaming.StreamingAnalyticsApplication");
            logger.info("StreamingAnalyticsApplication class found successfully");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("StreamingAnalyticsApplication class not found", e);
        }
    }

    @Test
    void sparkAnalyzerClassExists() {
        // Verify that the Spark analyzer class can be loaded
        try {
            Class.forName("com.enterprise.java.streaming.spark.SparkStreamAnalyzer");
            logger.info("SparkStreamAnalyzer class found successfully");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("SparkStreamAnalyzer class not found", e);
        }
    }

    @Test
    void flinkProcessorClassExists() {
        // Verify that the Flink processor class can be loaded
        try {
            Class.forName("com.enterprise.java.streaming.flink.FlinkStreamProcessor");
            logger.info("FlinkStreamProcessor class found successfully");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("FlinkStreamProcessor class not found", e);
        }
    }

    @Test
    void dashboardClassExists() {
        // Verify that the dashboard class can be loaded
        try {
            Class.forName("com.enterprise.java.streaming.dashboard.AnalyticsDashboard");
            logger.info("AnalyticsDashboard class found successfully");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("AnalyticsDashboard class not found", e);
        }
    }
}