package uk.gov.netz.api.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.configuration.domain.ConfigurationEntity;
import uk.gov.netz.api.configuration.domain.ConfigurationValueType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ ObjectMapper.class })
public class ConfigurationRepositoryIT extends AbstractContainerBaseTest {

	@Autowired
	ConfigurationRepository cut;

	@Autowired
	EntityManager em;

	@Test
	void findByKey() {
		String key = "key1";
		ConfigurationEntity conf1 = ConfigurationEntity.builder()
				.key(key).value("val1").type(ConfigurationValueType.STRING)
				.build();
		em.persist(conf1);
		
		em.flush();
		em.clear();

		Optional<ConfigurationEntity> result = cut.findByKey("anotherKey");
		assertThat(result).isEmpty();
		
		result = cut.findByKey(key);
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(conf1);
	}
	
	@Test
	void findByKeyStartingWith() {
		String prefix = "ui.";
		ConfigurationEntity conf1 = ConfigurationEntity.builder()
				.key("ui.features.feat1").value("val1").type(ConfigurationValueType.STRING)
				.build();
		em.persist(conf1);
		ConfigurationEntity conf2 = ConfigurationEntity.builder()
				.key("ui.analytics.feat1").value("val2").type(ConfigurationValueType.STRING)
				.build();
		em.persist(conf2);
		ConfigurationEntity conf3 = ConfigurationEntity.builder()
				.key("conf3").value("val3").type(ConfigurationValueType.STRING)
				.build();
		em.persist(conf3);
		
		em.flush();
		em.clear();
		
		List<ConfigurationEntity> result = cut.findByKeyStartingWith(prefix);
		
		assertThat(result).extracting(ConfigurationEntity::getKey).containsExactlyInAnyOrder("ui.features.feat1",
				"ui.analytics.feat1");
		
	}

}
