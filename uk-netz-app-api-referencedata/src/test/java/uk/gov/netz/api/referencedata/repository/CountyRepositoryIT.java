package uk.gov.netz.api.referencedata.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.referencedata.domain.County;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class CountyRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private CountyRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByNameWhenExists() {
        final County county = new County(1L, "AAAA");

        entityManager.persist(county);
        flushAndClear();

        final Optional<County> actual = repository.findById(county.getId());
        assertTrue(actual.isPresent());
        assertEquals(county.getName(), actual.get().getName());
        assertEquals(county.getId(), actual.get().getId());
    }

    @Test
    void findByNameWhenDoesNotExists() {
        final County county = new County(10L, "AAAA");

        entityManager.persist(county);
        flushAndClear();

        final Optional<County> actual = repository.findById(1000L);
        assertTrue(actual.isEmpty());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
