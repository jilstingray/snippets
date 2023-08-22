package org.jilstingray.kafka.model;

import jakarta.persistence.*;
import lombok.Data;

// This is a sample JPA entity class.
// Define your own classes in this package.
@Data
@Entity
@Table(name = "sample")
public class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "age")
    private int age;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;
}
