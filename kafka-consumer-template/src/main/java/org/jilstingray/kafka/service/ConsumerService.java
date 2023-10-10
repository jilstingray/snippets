package org.jilstingray.kafka.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jilstingray.kafka.model.Message;
import org.jilstingray.kafka.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService
{
    @Autowired
    private MessageRepository messageRepository;

    public void consume(List<ConsumerRecord<String, byte[]>> records)
    {
        List<Message> messages = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> record : records) {
            String json = new String(record.value());
            json = json.replaceAll("\"\":\"\\S+\",", "");
            Gson gson = new Gson();
            Message data;
            try {
                data = gson.fromJson(json, Message.class);
            }
            catch (Exception e) {
                log.error("Error parsing json: " + json);
                continue;
            }
            if (data != null) {
                messages.add(data);
            }
            else {
                log.warn("Empty data received...");
            }
        }
        messageRepository.saveAll(messages);
    }
}
