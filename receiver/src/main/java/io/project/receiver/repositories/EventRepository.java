package io.project.receiver.repositories;

import io.project.receiver.domain.PaymentEvent;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CrudRepository<PaymentEvent, String> {

    Optional<PaymentEvent> findTop1000ByStatus(String status);

}
