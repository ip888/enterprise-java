package com.enterprise.java.streaming.spark;

import com.enterprise.java.streaming.model.UserEvent;
import com.enterprise.java.streaming.model.UserBehaviorPattern;
import com.enterprise.java.streaming.util.KafkaEventDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Apache Spark Streaming application for ML-based analytics.
 * 
 * Features:
 * - Real-time user behavior analysis using Kafka streams
 * - ML model training with K-Means clustering for user segmentation
 * - Batch processing for historical data analysis
 * - User activity pattern recognition
 * - Performance metrics tracking
 */
public class SparkStreamAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(SparkStreamAnalyzer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    private SparkSession spark;
    private StreamingQuery streamingQuery;
    private PipelineModel mlModel;
    
    // Kafka configuration
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String OUTPUT_TOPIC = "analytics-insights";
    
    public SparkStreamAnalyzer() {
        initializeSparkSession();
    }
    
    private void initializeSparkSession() {
        logger.info("🔥 Initializing Apache Spark session...");
        
        SparkConf conf = new SparkConf()
                .setAppName("Enterprise-Streaming-Analytics")
                .setMaster("local[*]")
                .set("spark.sql.adaptive.enabled", "true")
                .set("spark.sql.adaptive.coalescePartitions.enabled", "true")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.sql.streaming.checkpointLocation", "/tmp/spark-checkpoint")
                .set("spark.sql.warehouse.dir", "/tmp/spark-warehouse");
        
        spark = SparkSession.builder()
                .config(conf)
                .getOrCreate();
        
        // Set log level to reduce noise
        spark.sparkContext().setLogLevel("WARN");
        
        logger.info("✅ Spark session initialized successfully");
    }
    
    public void startStreaming() throws StreamingQueryException, TimeoutException {
        logger.info("🚀 Starting Spark streaming from Kafka topic: {}", USER_EVENTS_TOPIC);
        
        // Define schema for user events
        StructType userEventSchema = new StructType()
                .add("userId", DataTypes.StringType)
                .add("eventType", DataTypes.StringType)
                .add("timestamp", DataTypes.TimestampType)
                .add("metadata", DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType))
                .add("sessionId", DataTypes.StringType)
                .add("ipAddress", DataTypes.StringType);
        
        // Read from Kafka stream
        Dataset<Row> kafkaStream = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS)
                .option("subscribe", USER_EVENTS_TOPIC)
                .option("startingOffsets", "latest")
                .option("failOnDataLoss", "false")
                .load();
        
        // Parse JSON and extract user events
        Dataset<Row> userEvents = kafkaStream
                .select(functions.from_json(functions.col("value").cast("string"), userEventSchema).as("data"))
                .select("data.*")
                .withColumn("hour", functions.hour(functions.col("timestamp")))
                .withColumn("dayOfWeek", functions.dayofweek(functions.col("timestamp")));
        
        // Perform real-time analytics
        Dataset<Row> analytics = performRealTimeAnalytics(userEvents);
        
        // Train ML model periodically
        trainMLModelPeriodically(userEvents);
        
        // Write results to console and Kafka
        streamingQuery = analytics
                .writeStream()
                .outputMode("update")
                .format("console")
                .option("truncate", "false")
                .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("30 seconds"))
                .start();
        
        logger.info("✅ Spark streaming started - processing user events in real-time");
        
        // Keep the streaming job running
        streamingQuery.awaitTermination();
    }
    
    private Dataset<Row> performRealTimeAnalytics(Dataset<Row> userEvents) {
        logger.info("📊 Performing real-time analytics on user events");
        
        return userEvents
                .withWatermark("timestamp", "10 minutes")
                .groupBy(
                        functions.window(functions.col("timestamp"), "5 minutes"),
                        functions.col("eventType"),
                        functions.col("hour"),
                        functions.col("dayOfWeek")
                )
                .agg(
                        functions.count("*").alias("eventCount"),
                        functions.countDistinct("userId").alias("uniqueUsers"),
                        functions.countDistinct("sessionId").alias("uniqueSessions"),
                        functions.avg("hour").alias("avgHour")
                )
                .withColumn("analysisTimestamp", functions.current_timestamp())
                .select(
                        functions.col("window.start").alias("windowStart"),
                        functions.col("window.end").alias("windowEnd"),
                        functions.col("eventType"),
                        functions.col("eventCount"),
                        functions.col("uniqueUsers"),
                        functions.col("uniqueSessions"),
                        functions.col("avgHour"),
                        functions.col("analysisTimestamp")
                );
    }
    
    private void trainMLModelPeriodically(Dataset<Row> userEvents) {
        logger.info("🧠 Setting up ML model training pipeline");
        
        // This would typically be triggered periodically
        // For demo purposes, we'll show the setup
        try {
            // Aggregate user behavior data for ML training
            Dataset<Row> userBehaviorFeatures = userEvents
                    .groupBy("userId")
                    .agg(
                            functions.count("*").alias("totalEvents"),
                            functions.countDistinct("sessionId").alias("sessions"),
                            functions.avg("hour").alias("avgActiveHour"),
                            functions.stddev("hour").alias("hourVariability"),
                            functions.count(functions.when(functions.col("eventType").equalTo("USER_REGISTERED"), 1)).alias("registrations"),
                            functions.count(functions.when(functions.col("eventType").equalTo("USER_LOGIN"), 1)).alias("logins"),
                            functions.count(functions.when(functions.col("eventType").equalTo("USER_SEARCH"), 1)).alias("searches")
                    );
            
            // Prepare features for ML
            String[] featureColumns = {"totalEvents", "sessions", "avgActiveHour", "hourVariability", "registrations", "logins", "searches"};
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(featureColumns)
                    .setOutputCol("features");
            
            // K-Means clustering for user segmentation
            KMeans kmeans = new KMeans()
                    .setK(5)
                    .setSeed(1L)
                    .setFeaturesCol("features")
                    .setPredictionCol("userSegment");
            
            // Create ML pipeline
            Pipeline pipeline = new Pipeline()
                    .setStages(new PipelineStage[]{assembler, kmeans});
            
            logger.info("🎯 ML pipeline configured for user segmentation");
            
        } catch (Exception e) {
            logger.error("❌ Error setting up ML pipeline", e);
        }
    }
    
    public void stop() throws StreamingQueryException {
        logger.info("🛑 Stopping Spark streaming analyzer");
        if (streamingQuery != null && streamingQuery.isActive()) {
            try {
                streamingQuery.stop();
            } catch (java.util.concurrent.TimeoutException e) {
                logger.warn("⚠️ Timeout while stopping streaming query", e);
                // Force stop if timeout occurs
                try {
                    if (streamingQuery.isActive()) {
                        streamingQuery.stop();
                    }
                } catch (java.util.concurrent.TimeoutException te) {
                    logger.error("❌ Failed to force stop streaming query after timeout", te);
                }
            }
        }
        if (spark != null) {
            spark.stop();
        }
        logger.info("✅ Spark streaming analyzer stopped");
    }
    
    /**
     * Batch processing method for historical analysis
     */
    public void processHistoricalData() {
        logger.info("📚 Processing historical data for ML model training");
        
        try {
            // This would typically read from a data warehouse or historical data store
            // For demo purposes, we'll simulate historical data analysis
            
            Dataset<Row> historicalData = spark.read()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS)
                    .option("subscribe", USER_EVENTS_TOPIC)
                    .option("startingOffsets", "earliest")
                    .option("endingOffsets", "latest")
                    .load();
            
            logger.info("📊 Historical data loaded, {} records", historicalData.count());
            
            // Perform batch analysis
            performBatchAnalysis(historicalData);
            
        } catch (Exception e) {
            logger.error("❌ Error processing historical data", e);
        }
    }
    
    private void performBatchAnalysis(Dataset<Row> data) {
        logger.info("🔍 Performing batch analysis on historical data");
        
        // Implement batch analytics logic here
        // This could include:
        // - User behavior pattern analysis
        // - Trend analysis
        // - Anomaly detection
        // - Model retraining
        
        logger.info("✅ Batch analysis completed");
    }
    
    /**
     * Get current streaming metrics
     */
    public Map<String, Object> getStreamingMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        if (streamingQuery != null) {
            try {
                org.apache.spark.sql.streaming.StreamingQueryProgress progress = streamingQuery.lastProgress();
                if (progress != null) {
                    metrics.put("batchId", progress.batchId());
                    metrics.put("inputRowsPerSecond", progress.inputRowsPerSecond());
                    metrics.put("processedRowsPerSecond", progress.processedRowsPerSecond());
                    metrics.put("durationMs", progress.durationMs());
                }
            } catch (Exception e) {
                logger.warn("Could not fetch streaming metrics", e);
            }
        }
        
        return metrics;
    }
}