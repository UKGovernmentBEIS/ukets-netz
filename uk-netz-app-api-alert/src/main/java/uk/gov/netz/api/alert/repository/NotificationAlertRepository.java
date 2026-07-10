package uk.gov.netz.api.alert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.netz.api.alert.domain.NotificationAlert;

public interface NotificationAlertRepository extends JpaRepository<NotificationAlert, Long> {

	List<NotificationAlert> findAllByOrderByActiveFromAsc();
}
