package uk.gov.netz.api.configuration.service.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.configuration.domain.ui.UIApplicationProperties;
import uk.gov.netz.api.configuration.domain.ui.UIConfigurationPropertiesDTO;
import uk.gov.netz.api.configuration.service.ConfigurationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UIConfigurationPropertiesResolverTest {

	@InjectMocks
    private UIConfigurationPropertiesResolver cut;

    @Mock
    private UIApplicationProperties uiApplicationProperties;

    @Mock
    private ConfigurationService configurationService;
    
    @Test
    void resolve() {
		Map<String, Object> uiConfigs = new HashMap<String, Object>() {
			{
				put("ui.features.key1", Boolean.TRUE);
				put("ui.features.key2", Boolean.TRUE);
				put("ui.another", "another_db_config");
			}
		};
		
		Map<String, String> analytics = new HashMap<String, String>() {
			{
				put("key1", "analytics1");
				put("key2", "analytics2");
			}
		};
		
		Map<String, Boolean> featuresApplicationProperties = new HashMap<String, Boolean>() {
			{
				put("key2", Boolean.FALSE);
				put("key3", Boolean.TRUE);
			}
		};

		Map<String, String> propertiesApplicationProperties = new HashMap<String, String>() {
			{
				put("key3", "test1");
				put("key4", "test2");
			}
		};
		
		String keycloakServerUrl = "keycloakServerUrl";
		
		when(configurationService.getUIConfigurations()).thenReturn(uiConfigs);
		when(uiApplicationProperties.getKeycloakServerUrl()).thenReturn(keycloakServerUrl);
		when(uiApplicationProperties.getAnalytics()).thenReturn(analytics);
		when(uiApplicationProperties.getFeatures()).thenReturn(featuresApplicationProperties);
		when(uiApplicationProperties.getProperties()).thenReturn(propertiesApplicationProperties);

		UIConfigurationPropertiesDTO result = cut.resolve();
		
		assertThat(result).isEqualTo(UIConfigurationPropertiesDTO.builder()
				.features(Map.of(
						"key1", Boolean.TRUE,
						"key2", Boolean.TRUE,
						"key3", Boolean.TRUE
						))
				.analytics(Map.of(
						"key1", "analytics1",
						"key2", "analytics2"
						))
				.properties(Map.of(
						"key3", "test1",
						"key4", "test2"
				))
				.keycloakServerUrl(keycloakServerUrl)
				.build());
		
		verify(configurationService, times(1)).getUIConfigurations();
		verify(uiApplicationProperties, times(1)).getKeycloakServerUrl();
		verify(uiApplicationProperties, times(1)).getAnalytics();
		verify(uiApplicationProperties, times(1)).getFeatures();
		verify(uiApplicationProperties, times(1)).getProperties();
    }
	
}
