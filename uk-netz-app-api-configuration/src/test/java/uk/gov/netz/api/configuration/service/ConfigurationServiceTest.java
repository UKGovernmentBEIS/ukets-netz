package uk.gov.netz.api.configuration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.configuration.constants.ConfigurationConstants;
import uk.gov.netz.api.configuration.domain.ConfigurationDTO;
import uk.gov.netz.api.configuration.domain.ConfigurationEntity;
import uk.gov.netz.api.configuration.domain.ConfigurationValueType;
import uk.gov.netz.api.configuration.repository.ConfigurationRepository;

@ExtendWith(MockitoExtension.class)
public class ConfigurationServiceTest {

	@InjectMocks
    private ConfigurationService cut;

    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Test
    void getConfigurationByKey_string_value() {
    	String key = "key";
    	ConfigurationEntity confEntity = ConfigurationEntity.builder()
    			.key(key).value("val").type(ConfigurationValueType.STRING)
    			.build();
    	
    	ConfigurationDTO confDTO = ConfigurationDTO.builder()
    			.key(key).value("val")
    			.build();
    	when(configurationRepository.findByKey(key)).thenReturn(Optional.of(confEntity));
    	
    	Optional<ConfigurationDTO> result = cut.getConfigurationByKey(key);
    	assertThat(result).isPresent();
    	assertThat(result.get()).isEqualTo(confDTO);
    	
    	verify(configurationRepository, times(1)).findByKey(key);
    }
    
    @Test
    void getConfigurationByKey_boolean_value() {
    	String key = "key";
    	ConfigurationEntity confEntity = ConfigurationEntity.builder()
    			.key(key).value("true").type(ConfigurationValueType.BOOLEAN)
    			.build();
    	
    	ConfigurationDTO confDTO = ConfigurationDTO.builder()
    			.key(key).value(true)
    			.build();
    	when(configurationRepository.findByKey(key)).thenReturn(Optional.of(confEntity));
    	
    	Optional<ConfigurationDTO> result = cut.getConfigurationByKey(key);
    	assertThat(result).isPresent();
    	assertThat(result.get()).isEqualTo(confDTO);
    	
    	verify(configurationRepository, times(1)).findByKey(key);
    }
    
    @Test
    void getConfigurationByKey_integer_value() {
    	String key = "key";
    	ConfigurationEntity confEntity = ConfigurationEntity.builder()
    			.key(key).value("3").type(ConfigurationValueType.INTEGER)
    			.build();
    	
    	ConfigurationDTO confDTO = ConfigurationDTO.builder()
    			.key(key).value(3)
    			.build();
    	when(configurationRepository.findByKey(key)).thenReturn(Optional.of(confEntity));
    	
    	Optional<ConfigurationDTO> result = cut.getConfigurationByKey(key);
    	assertThat(result).isPresent();
    	assertThat(result.get()).isEqualTo(confDTO);
    	
    	verify(configurationRepository, times(1)).findByKey(key);
    }
    
    @Test
    void getUIConfigurations() {
    	ConfigurationEntity uiConfEntity1 = ConfigurationEntity.builder()
    			.key("ui.foo1").value("true").type(ConfigurationValueType.BOOLEAN)
    			.build();
    	ConfigurationEntity uiConfEntity2 = ConfigurationEntity.builder()
    			.key("ui.foo2").value("bar").type(ConfigurationValueType.STRING)
    			.build();
    	ConfigurationEntity uiConfEntity3 = ConfigurationEntity.builder()
    			.key("ui.foo3").value(null).type(ConfigurationValueType.STRING)
    			.build();
    	
		when(configurationRepository.findByKeyStartingWith(ConfigurationConstants.UI_PREFIX))
				.thenReturn(List.of(uiConfEntity1, uiConfEntity2, uiConfEntity3));

		Map<String, Object> result = cut.getUIConfigurations();
		
		assertThat(result).containsExactlyInAnyOrderEntriesOf(new HashMap<String, Object>() {
			{
				put("ui.foo1", Boolean.TRUE);
				put("ui.foo2", "bar");
				put("ui.foo3", null);
			}
		});
		
		verify(configurationRepository, times(1)).findByKeyStartingWith(ConfigurationConstants.UI_PREFIX);
    }
    
}
