package uk.gov.netz.api.configuration.service.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.configuration.constants.ConfigurationConstants;
import uk.gov.netz.api.configuration.domain.ui.UIApplicationProperties;
import uk.gov.netz.api.configuration.domain.ui.UIConfigurationPropertiesDTO;
import uk.gov.netz.api.configuration.service.ConfigurationService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UIConfigurationPropertiesResolver {
	
	private final UIApplicationProperties uiApplicationProperties;
	private final ConfigurationService configurationService;

	public UIConfigurationPropertiesDTO resolve() {
		final Map<String, Object> uiConfigsDb = configurationService.getUIConfigurations();
		
		final Map<String, Boolean> features = buildFeaturesMap(uiConfigsDb);

		return UIConfigurationPropertiesDTO.builder()
				.features(features)
				.analytics(uiApplicationProperties.getAnalytics())
				.properties(uiApplicationProperties.getProperties())
				.keycloakServerUrl(uiApplicationProperties.getKeycloakServerUrl())
				.build();
	}

	private Map<String, Boolean> buildFeaturesMap(final Map<String, Object> uiConfigsDb) {
		final Map<String, Boolean> featuresApplicationProperties = uiApplicationProperties.getFeatures();
		final Map<String, Boolean> featuresDbProperties = uiConfigsDb.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(ConfigurationConstants.UI_FEATURES_PREFIX))
				.collect(HashMap<String, Boolean>::new,
						(map, configuration) -> 
							map.put(
								configuration.getKey().substring(ConfigurationConstants.UI_FEATURES_PREFIX.length()),
								(Boolean) configuration.getValue()),
						HashMap::putAll);
		final Map<String, Boolean> featuresAll = new HashMap<>(featuresApplicationProperties);
		featuresDbProperties.forEach((dbKey, dbValue) -> featuresAll.merge(dbKey, dbValue, (all, db) -> db));
		return featuresAll;
	}
}
