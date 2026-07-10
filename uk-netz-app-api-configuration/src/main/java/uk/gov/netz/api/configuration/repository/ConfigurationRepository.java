package uk.gov.netz.api.configuration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.netz.api.configuration.domain.ConfigurationEntity;

public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, String> {

	Optional<ConfigurationEntity> findByKey(String key);
	
	List<ConfigurationEntity> findByKeyStartingWith(String prefix);
	
}
