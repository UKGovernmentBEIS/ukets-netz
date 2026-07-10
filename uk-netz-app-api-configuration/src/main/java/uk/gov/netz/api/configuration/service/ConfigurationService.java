package uk.gov.netz.api.configuration.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.configuration.constants.ConfigurationConstants;
import uk.gov.netz.api.configuration.domain.ConfigurationDTO;
import uk.gov.netz.api.configuration.domain.ConfigurationValueType;
import uk.gov.netz.api.configuration.repository.ConfigurationRepository;

@Service
@RequiredArgsConstructor
public class ConfigurationService {
	
	private final ConfigurationRepository configurationRepository;

	public Optional<ConfigurationDTO> getConfigurationByKey(String key) {
		return configurationRepository.findByKey(key)
				.map(configuration -> resolveTypeValue(configuration.getType(), configuration.getValue()))
				.map(resolvedConfiguration -> ConfigurationDTO.builder().key(key).value(resolvedConfiguration).build());
	}

	public Map<String, Object> getUIConfigurations() {
		return configurationRepository.findByKeyStartingWith(ConfigurationConstants.UI_PREFIX).stream().collect(HashMap<String, Object>::new,
				(map, configuration) -> map.put(configuration.getKey(),
						resolveTypeValue(configuration.getType(), configuration.getValue())),
				HashMap::putAll);
	}

	private Object resolveTypeValue(ConfigurationValueType type, String valueAsString) {
		if(valueAsString == null) {
			return null;
		}
		
		return switch (type) {
		case STRING: {
			yield valueAsString;
		}
		case BOOLEAN: {
			yield Boolean.valueOf(valueAsString);
		}
		case INTEGER: {
			yield Integer.valueOf(valueAsString);
		}
		default:
			throw new IllegalArgumentException("Type not supported yet: " + type);
		};
	}

}
