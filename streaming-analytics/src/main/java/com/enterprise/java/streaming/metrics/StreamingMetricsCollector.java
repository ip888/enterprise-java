package com.enterprise.java.streaming.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Production-ready metrics collector for streaming analytics
 * Replaces custom dashboard with industry-standard Prometheus metrics
 */
public class StreamingMetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(StreamingMetricsCollector.class);
    
    private final MeterRegistry meterRegistry;
    
    // Counters for business metrics
    private final Counter totalEventsProcessed;
    private final Counter suspiciousLoginAttempts;
    private final Counter alertsTriggered;
    
    // Gauges for current state
    private final AtomicLong uniqueUsers = new AtomicLong(0);
    private final AtomicLong activeStreams = new AtomicLong(0);
    private final AtomicLong throughputPerSecond = new AtomicLong(0);
    
    // Timers for performance metrics
    private final Timer processingTime;
    
    public StreamingMetricsCollector() {
        this.meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
        // Initialize counters
        this.totalEventsProcessed = Counter.builder("streaming_events_processed_total")
                .description("Total number of events processed")
                .register(meterRegistry);
                
        this.suspiciousLoginAttempts = Counter.builder("suspicious_login_attempts_total")
                .description("Total number of suspicious login attempts detected")
                .register(meterRegistry);
                
        this.alertsTriggered = Counter.builder("streaming_alerts_triggered_total")
                .description("Total number of alerts triggered")
                .register(meterRegistry);
        
        // Initialize gauges
        meterRegistry.gauge("streaming_unique_users", uniqueUsers);
        meterRegistry.gauge("streaming_active_streams", activeStreams);
        meterRegistry.gauge("streaming_throughput_per_second", throughputPerSecond);
        
        // Initialize timers
        this.processingTime = Timer.builder("streaming_processing_duration_seconds")
                .description("Time taken to process streaming events")
                .register(meterRegistry);
        
        logger.info("✅ Streaming metrics collector initialized with Prometheus registry");
    }
    
    // Business metrics methods
    public void incrementEventsProcessed() {
        totalEventsProcessed.increment();
    }
    
    public void incrementEventsProcessed(long count) {
        totalEventsProcessed.increment(count);
    }
    
    public void incrementSuspiciousLogins() {
        suspiciousLoginAttempts.increment();
    }
    
    public void incrementAlertsTriggered() {
        alertsTriggered.increment();
    }
    
    public void updateUniqueUsers(long count) {
        uniqueUsers.set(count);
    }
    
    public void updateActiveStreams(long count) {
        activeStreams.set(count);
    }
    
    public void updateThroughput(long eventsPerSecond) {
        throughputPerSecond.set(eventsPerSecond);
    }
    
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordProcessingTime(Timer.Sample sample) {
        sample.stop(processingTime);
    }
    
    // Get Prometheus metrics endpoint content
    public String getPrometheusMetrics() {
        if (meterRegistry instanceof PrometheusMeterRegistry) {
            return ((PrometheusMeterRegistry) meterRegistry).scrape();
        }
        return "";
    }
    
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
    
    // Simulate realistic metrics for demo
    public void simulateRealisticMetrics() {
        incrementEventsProcessed(15430);
        updateUniqueUsers(1247);
        updateActiveStreams(3);
        incrementAlertsTriggered();
        updateThroughput(2340);
        incrementSuspiciousLogins();
        
        logger.debug("📊 Simulated realistic metrics for demo purposes");
    }
}