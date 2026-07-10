package uk.gov.netz.api.alert.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.alert.domain.NotificationAlert;
import uk.gov.netz.api.common.AbstractContainerBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class})
public class NotificationAlertRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    NotificationAlertRepository cut;

    @Autowired
    EntityManager em;

    @Test
    void findAllByOrderByActiveFromAsc() {
    	NotificationAlert alert1 = NotificationAlert.builder()
    			.subject("subject1")
    			.body("body1")
    			.activeFrom(LocalDateTime.now().minusDays(8))
    			.activeUntil(LocalDateTime.now().plusDays(8))
    			.createdBy("author1")
    			.creationDate(LocalDateTime.now())
    			.build();
    	em.persist(alert1);
    	
    	NotificationAlert alert2 = NotificationAlert.builder()
    			.subject("subject2")
    			.body("body2")
    			.activeFrom(LocalDateTime.now().minusDays(9))
    			.activeUntil(LocalDateTime.now().plusDays(9))
    			.createdBy("author2")
    			.creationDate(LocalDateTime.now())
    			.build();
    	em.persist(alert2);
    	
    	NotificationAlert alert3 = NotificationAlert.builder()
    			.subject("subject3")
    			.body("body3")
    			.activeFrom(LocalDateTime.now().minusDays(10))
    			.activeUntil(LocalDateTime.now().plusDays(10))
    			.createdBy("author3")
    			.creationDate(LocalDateTime.now())
    			.build();
    	em.persist(alert3);
    	
    	em.flush();
    	em.clear();

    	List<NotificationAlert> result = cut.findAllByOrderByActiveFromAsc();
    	
    	assertThat(result).extracting(NotificationAlert::getId).containsExactly(alert3.getId(), alert2.getId(), alert1.getId());
    }
}