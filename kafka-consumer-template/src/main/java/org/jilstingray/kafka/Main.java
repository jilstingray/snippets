package org.jilstingray.kafka;

import lombok.extern.slf4j.Slf4j;
import org.jilstingray.kafka.config.KafkaConfig;
import org.jilstingray.kafka.kafka.MultiThreadConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private KafkaConfig kafkaConfig;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Starting consumer...");
        MultiThreadConsumer consumer = new MultiThreadConsumer(
                kafkaConfig.getBootstrapServers(),
                kafkaConfig.getTopic(),
                kafkaConfig.getGroupId(),
                kafkaConfig.getConsumerId(),
                kafkaConfig.getMaxPollRecords()
        );
        try {
            while (true) {
                consumer.run();
            }
        } finally {
            consumer.close();
        }
    }
}