package org.jilstingray.kafka.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jilstingray.kafka.model.Sample;
import org.jilstingray.kafka.repository.SampleRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {
    private final SampleRepository sampleRepository;

    public void consume(ConsumerRecord<String, byte[]> record) {
        String json = new String(record.value());
        json = json.replaceAll("\"\":\"\\S+\",", "");
        // This is an example of parsing JSON with Gson.
        // Do anything you want here...
        Gson gson = new Gson();
        Sample data = gson.fromJson(json, Sample.class);
        if (data != null) {
            sampleRepository.save(data);
        } else {
            log.warn("Empty data received...");
        }
    }
}
