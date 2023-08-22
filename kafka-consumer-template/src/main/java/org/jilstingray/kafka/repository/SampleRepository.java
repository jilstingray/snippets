package org.jilstingray.kafka.repository;

import org.jilstingray.kafka.model.Sample;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleRepository extends CrudRepository<Sample, Long> {
}
