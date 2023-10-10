package org.jilstingray.kafka.repository;

import org.jilstingray.kafka.model.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository
        extends CrudRepository<Message, Long>
{
}
