package org.jilstingray.kafka.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

@Entity
@Data
public class Message implements Persistable<Long>, Serializable
{
    private static final long serialVersionUID = 2225926240419540529L;

    @Id
    private Long id;

    // keep the same as the json field name
    private String username;
    private Integer age;
    @Embedded
    private Address address;
    private String email;

    @Override
    public boolean isNew() {
        return true;
    }
}
