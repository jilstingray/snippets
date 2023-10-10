package org.jilstingray.kafka.runner;

import lombok.extern.slf4j.Slf4j;
import org.jilstingray.kafka.config.KafkaConfig;
import org.jilstingray.kafka.kafka.MultiThreadConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandLineRunnerImpl
        implements CommandLineRunner
{
    @Autowired
    private KafkaConfig kafkaConfig;

    @Override
    public void run(String... args)
    {
        log.info("Starting consumer...");
        MultiThreadConsumer consumer = new MultiThreadConsumer(
                kafkaConfig.getBootstrapServers(),
                kafkaConfig.getTopic(),
                kafkaConfig.getGroupId(),
                kafkaConfig.getConsumerId(),
                kafkaConfig.getMaxPollRecords());
        try {
            while (true) {
                consumer.run();
            }
        }
        finally {
            consumer.close();
        }
    }
}
