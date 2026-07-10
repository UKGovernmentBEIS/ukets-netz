package uk.gov.netz.api.competentauthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class})
public class CompetentAuthorityRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private CompetentAuthorityRepository<CompetentAuthority> competentAuthorityRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findById() {
    	CompetentAuthority ca = CompetentAuthority.builder().id(CompetentAuthorityEnum.ENGLAND).build();
    	em.persist(ca);
    	
    	em.flush();
    	em.clear();

		CompetentAuthority result = competentAuthorityRepository.findById(CompetentAuthorityEnum.ENGLAND).get();
    	assertEquals(result.getId(), CompetentAuthorityEnum.ENGLAND);
    }
}