package org.jilstingray.kafka.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String province;
    private String city;
}
