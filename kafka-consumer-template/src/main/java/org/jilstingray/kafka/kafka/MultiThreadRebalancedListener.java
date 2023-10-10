package org.jilstingray.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MultiThreadRebalancedListener
        implements ConsumerRebalanceListener
{
    private final Consumer<String, byte[]> consumer;
    private final Map<TopicPartition, ConsumerWorker> outstandingWorkers;
    private final Map<TopicPartition, OffsetAndMetadata> offsets;

    public MultiThreadRebalancedListener(Consumer<String, byte[]> consumer,
            Map<TopicPartition, ConsumerWorker> outstandingWorkers,
            Map<TopicPartition, OffsetAndMetadata> offsets)
    {
        this.consumer = consumer;
        this.outstandingWorkers = outstandingWorkers;
        this.offsets = offsets;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions)
    {
        Map<TopicPartition, ConsumerWorker> stoppedWorkers = new HashMap<>();
        for (TopicPartition partition : partitions) {
            ConsumerWorker worker = outstandingWorkers.remove(partition);
            if (worker != null) {
                worker.close();
                stoppedWorkers.put(partition, worker);
            }
        }

        stoppedWorkers.forEach((tp, worker) -> {
            long offset = worker.waitForCompletion(100, TimeUnit.MILLISECONDS);
            if (offset > 0L) {
                offsets.put(tp, new OffsetAndMetadata(offset));
            }
        });

        Map<TopicPartition, OffsetAndMetadata> revokedOffsets = new HashMap<>();
        partitions.forEach(tp -> {
            OffsetAndMetadata offset = offsets.remove(tp);
            if (offset != null) {
                revokedOffsets.put(tp, offset);
            }
        });

        try {
            consumer.commitSync(revokedOffsets);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions)
    {
        consumer.resume(partitions);
    }
}