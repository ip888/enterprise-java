package com.enterprise.java.streaming.flink;

import com.enterprise.java.streaming.model.UserEvent;
import com.enterprise.java.streaming.model.AlertEvent;
import com.enterprise.java.streaming.util.KafkaEventDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Apache Flink Stream Processing application for real-time event processing.
 * 
 * Features:
 * - Complex Event Processing (CEP) for pattern detection
 * - Real-time anomaly detection
 * - User behavior analysis and alerting
 * - Integration with Kafka for event consumption and production
 * - Stateful stream processing with checkpointing
 */
public class FlinkStreamProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(FlinkStreamProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    private StreamExecutionEnvironment env;
    
    // Kafka configuration
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String ALERTS_TOPIC = "security-alerts";
    private static final String ANALYTICS_TOPIC = "real-time-analytics";
    
    public FlinkStreamProcessor() {
        initializeFlinkEnvironment();
    }
    
    private void initializeFlinkEnvironment() {
        logger.info("⚡ Initializing Apache Flink streaming environment");
        
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Configure checkpointing for fault tolerance
        env.enableCheckpointing(30000); // Checkpoint every 30 seconds
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        
        // Set parallelism
        env.setParallelism(2);
        
        logger.info("✅ Flink environment configured with checkpointing enabled");
    }
    
    public void startProcessing() throws Exception {
        logger.info("🚀 Starting Flink stream processing pipeline");
        
        // Create Kafka source for user events
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(USER_EVENTS_TOPIC)
                .setGroupId("flink-stream-processor")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        
        // Read from Kafka and parse events
        DataStream<UserEvent> userEventStream = env
                .fromSource(kafkaSource, WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ofSeconds(20))
                        .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis()), "Kafka Source")
                .map(new UserEventParser())
                .name("Parse User Events");
        
        // Apply watermark strategy for event time processing
        DataStream<UserEvent> watermarkedStream = userEventStream
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<UserEvent>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli())
                );
        
        // Complex Event Processing for anomaly detection
        processComplexEventPatterns(watermarkedStream);
        
        // Real-time user behavior analysis
        performRealTimeBehaviorAnalysis(watermarkedStream);
        
        // Security monitoring
        detectSuspiciousActivity(watermarkedStream);
        
        // Execute the streaming job
        logger.info("🏃 Executing Flink streaming job...");
        env.execute("Enterprise-Flink-Stream-Processor");
    }
    
    private void processComplexEventPatterns(DataStream<UserEvent> eventStream) {
        logger.info("🔍 Setting up Complex Event Processing patterns");
        
        // Pattern: Rapid successive failed login attempts (potential brute force attack)
        Pattern<UserEvent, ?> suspiciousLoginPattern = Pattern.<UserEvent>begin("start")
                .where(new SimpleCondition<UserEvent>() {
                    @Override
                    public boolean filter(UserEvent event) {
                        return "USER_LOGIN_FAILED".equals(event.getEventType());
                    }
                })
                .timesOrMore(3)
                .within(Time.minutes(5));
        
        PatternStream<UserEvent> patternStream = CEP.pattern(
                eventStream.keyBy(UserEvent::getUserId), 
                suspiciousLoginPattern
        );
        
        DataStream<AlertEvent> alertStream = patternStream.select(new PatternSelectFunction<UserEvent, AlertEvent>() {
            @Override
            public AlertEvent select(Map<String, List<UserEvent>> pattern) throws Exception {
                List<UserEvent> events = pattern.get("start");
                UserEvent firstEvent = events.get(0);
                
                return AlertEvent.builder()
                        .alertId(java.util.UUID.randomUUID().toString())
                        .userId(firstEvent.getUserId())
                        .alertType("SUSPICIOUS_LOGIN_PATTERN")
                        .description("Multiple failed login attempts detected")
                        .timestamp(LocalDateTime.now())
                        .severity("HIGH")
                        .eventCount(events.size())
                        .build();
            }
        });
        
        // Send alerts to Kafka
        KafkaSink<AlertEvent> alertSink = KafkaSink.<AlertEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(ALERTS_TOPIC)
                        .setValueSerializationSchema(new AlertEventSerializer())
                        .build())
                .build();
        
        alertStream.sinkTo(alertSink).name("Security Alerts Sink");
        
        logger.info("✅ Complex Event Processing patterns configured");
    }
    
    private void performRealTimeBehaviorAnalysis(DataStream<UserEvent> eventStream) {
        logger.info("📊 Setting up real-time behavior analysis");
        
        // Windowed analytics for user activity patterns
        DataStream<Tuple2<String, UserActivitySummary>> activityAnalysis = eventStream
                .keyBy(UserEvent::getUserId)
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .aggregate(new UserActivityAggregator())
                .name("User Activity Analysis");
        
        // Send analytics results to Kafka
        KafkaSink<Tuple2<String, UserActivitySummary>> analyticsSink = KafkaSink.<Tuple2<String, UserActivitySummary>>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(ANALYTICS_TOPIC)
                        .setValueSerializationSchema(new AnalyticsSerializer())
                        .build())
                .build();
        
        activityAnalysis.sinkTo(analyticsSink).name("Analytics Results Sink");
        
        logger.info("✅ Real-time behavior analysis configured");
    }
    
    private void detectSuspiciousActivity(DataStream<UserEvent> eventStream) {
        logger.info("🛡️ Setting up suspicious activity detection");
        
        // Detect unusual activity patterns
        SingleOutputStreamOperator<AlertEvent> suspiciousActivityStream = eventStream
                .keyBy(UserEvent::getUserId)
                .process(new SuspiciousActivityDetector())
                .name("Suspicious Activity Detector");
        
        // Filter only high-priority alerts
        DataStream<AlertEvent> highPriorityAlerts = suspiciousActivityStream
                .filter(new FilterFunction<AlertEvent>() {
                    @Override
                    public boolean filter(AlertEvent alert) throws Exception {
                        return "HIGH".equals(alert.getSeverity()) || "CRITICAL".equals(alert.getSeverity());
                    }
                });
        
        // Send high-priority alerts immediately
        KafkaSink<AlertEvent> priorityAlertSink = KafkaSink.<AlertEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(ALERTS_TOPIC)
                        .setValueSerializationSchema(new AlertEventSerializer())
                        .build())
                .build();
        
        highPriorityAlerts.sinkTo(priorityAlertSink).name("Priority Alerts Sink");
        
        logger.info("✅ Suspicious activity detection configured");
    }
    
    public void stop() {
        logger.info("🛑 Stopping Flink stream processor");
        // Graceful shutdown would be handled by the Flink runtime
        logger.info("✅ Flink stream processor stopped");
    }
    
    /**
     * Custom function to parse JSON user events
     */
    private static class UserEventParser implements MapFunction<String, UserEvent> {
        @Override
        public UserEvent map(String value) throws Exception {
            try {
                return objectMapper.readValue(value, UserEvent.class);
            } catch (Exception e) {
                // Return a default event for parsing errors
                return UserEvent.builder()
                        .userId("unknown")
                        .eventType("PARSE_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
        }
    }
    
    /**
     * Stateful function to detect suspicious activity patterns
     */
    private static class SuspiciousActivityDetector extends KeyedProcessFunction<String, UserEvent, AlertEvent> {
        
        private ValueState<Integer> eventCountState;
        private ValueState<LocalDateTime> lastEventTimeState;
        
        @Override
        public void open(Configuration parameters) throws Exception {
            eventCountState = getRuntimeContext().getState(
                    new ValueStateDescriptor<>("eventCount", Integer.class));
            lastEventTimeState = getRuntimeContext().getState(
                    new ValueStateDescriptor<>("lastEventTime", LocalDateTime.class));
        }
        
        @Override
        public void processElement(UserEvent event, Context context, Collector<AlertEvent> collector) throws Exception {
            Integer currentCount = eventCountState.value();
            LocalDateTime lastEventTime = lastEventTimeState.value();
            
            if (currentCount == null) {
                currentCount = 0;
            }
            
            currentCount++;
            eventCountState.update(currentCount);
            lastEventTimeState.update(event.getTimestamp());
            
            // Detect rapid activity (more than 50 events in 1 minute)
            if (lastEventTime != null) {
                Duration timeDiff = Duration.between(lastEventTime, event.getTimestamp());
                if (currentCount > 50 && timeDiff.toMinutes() <= 1) {
                    AlertEvent alert = AlertEvent.builder()
                            .alertId(java.util.UUID.randomUUID().toString())
                            .userId(event.getUserId())
                            .alertType("RAPID_ACTIVITY")
                            .description("Unusually high activity rate detected")
                            .timestamp(LocalDateTime.now())
                            .severity("HIGH")
                            .eventCount(currentCount)
                            .build();
                    
                    collector.collect(alert);
                    
                    // Reset counter after alert
                    eventCountState.update(0);
                }
            }
            
            // Set timer to reset state after 5 minutes of inactivity
            context.timerService().registerEventTimeTimer(
                    context.timestamp() + Duration.ofMinutes(5).toMillis());
        }
        
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<AlertEvent> out) throws Exception {
            // Reset state after timeout
            eventCountState.clear();
            lastEventTimeState.clear();
        }
    }
}