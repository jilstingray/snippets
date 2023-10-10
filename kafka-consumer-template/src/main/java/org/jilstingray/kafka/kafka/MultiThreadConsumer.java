package org.jilstingray.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class MultiThreadConsumer
{
    private final static Executor executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 10, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
    private final Map<TopicPartition, ConsumerWorker> outstandingWorkers = new HashMap<>();
    private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
    private final OffsetCommitCallback commitCallback = (offsets, exception) -> {
        if (exception != null) {
            log.error("Failed to commit offsets {}... Exception: {}", offsets, exception.getMessage());
        }
    };
    private final Consumer<String, byte[]> consumer;

    public MultiThreadConsumer(String servers, String topic, String groupId, String consumerId, String maxPollRecords)
    {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "86400000");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic), new MultiThreadRebalancedListener(consumer, outstandingWorkers, offsetsToCommit));
    }

    public void run()
    {
        distributeRecords(consumer.poll(Duration.ofMillis(100)));
        resumePartitions();
        commitAsync();
    }

    // send messages from different partitions to different threads
    private void distributeRecords(ConsumerRecords<String, byte[]> records)
    {
        records.partitions().forEach(tp -> {
            // 按分区获取消息
            log.info("Part {}: receiving {} messages", tp, records.count());
            List<ConsumerRecord<String, byte[]>> partitionedRecords = records.records(tp);
            ConsumerWorker worker = new ConsumerWorker(partitionedRecords);
            CompletableFuture.supplyAsync(worker::run, executor);
            outstandingWorkers.put(tp, worker);
        });
        consumer.pause(records.partitions());
    }

    // resume partitions that have been processed
    private void resumePartitions()
    {
        Set<TopicPartition> partitions = new HashSet<>();
        outstandingWorkers.forEach((tp, worker) -> {
            if (worker.isDone()) {
                log.info("Part {}: done", tp);
                partitions.add(tp);
                offsetsToCommit.put(tp, new OffsetAndMetadata(worker.getLatestOffset()));
            }
        });
        partitions.forEach(outstandingWorkers::remove);
        consumer.resume(partitions);
    }

    // async
    private void commitAsync()
    {
        if (!offsetsToCommit.isEmpty()) {
            log.info("Async commit offset: {}", offsetsToCommit);
            consumer.commitAsync(offsetsToCommit, commitCallback);
            offsetsToCommit.clear();
        }
    }

    // sync
    private void commitSync()
    {
        if (!offsetsToCommit.isEmpty()) {
            log.info("Sync commit offset: {}", offsetsToCommit);
            consumer.commitSync(offsetsToCommit);
            offsetsToCommit.clear();
        }
    }

    public void close()
    {
        commitSync();
        consumer.close();
        log.warn("Closing consumer...");
    }
}