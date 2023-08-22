package org.jilstingray.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jilstingray.kafka.service.ConsumerService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConsumerWorker {
    private final List<ConsumerRecord<String, byte[]>> consumerRecords;
    private final ReentrantLock lock = new ReentrantLock();
    private final long INVALID_COMMITTED_OFFSET = -1L;
    private final AtomicLong latestOffset = new AtomicLong(INVALID_COMMITTED_OFFSET);
    private final CompletableFuture<Long> future = new CompletableFuture<>();
    private volatile boolean started = false;
    private volatile boolean stopped = false;

    public ConsumerWorker(List<ConsumerRecord<String, byte[]>> consumerRecords) {
        this.consumerRecords = consumerRecords;
    }

    public boolean run() {
        lock.lock();
        if (stopped) {
            return false;
        }
        started = true;
        lock.unlock();
        for (ConsumerRecord<String, byte[]> record : consumerRecords) {
            if (stopped) {
                break;
            }
            handleRecord(record);
            if (latestOffset.get() < record.offset() + 1) {
                latestOffset.set(record.offset() + 1);
            }
        }
        return future.complete(latestOffset.get());
    }

    public long getLatestOffset() {
        return latestOffset.get();
    }

    private void handleRecord(ConsumerRecord<String, byte[]> record) {
        // do something...
        ConsumerService service = ContextAware.getBean(ConsumerService.class);
        try {
            service.consume(record);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    public void close() {
        lock.lock();
        this.stopped = true;
        if (!started) {
            future.complete(latestOffset.get());
        }
        lock.unlock();
    }

    public boolean isDone() {
        return future.isDone();
    }

    public long waitForCompletion(long timeout, TimeUnit timeUnit) {
        try {
            return future.get(timeout, timeUnit);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return INVALID_COMMITTED_OFFSET;
        }
    }
}