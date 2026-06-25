package cl.usm.tallerhdd.repositories;

import cl.usm.tallerhdd.entities.RegistroPesaje;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroPesajeRepository extends MongoRepository<RegistroPesaje, String> {
    List<RegistroPesaje> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
