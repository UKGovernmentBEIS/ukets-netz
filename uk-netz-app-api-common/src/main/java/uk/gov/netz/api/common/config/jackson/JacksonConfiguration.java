package uk.gov.netz.api.common.config.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import uk.gov.netz.api.common.config.LocalDateTimeDeserializerConverter;
import uk.gov.netz.api.common.config.LocalDateTimeSerializerConverter;

import java.time.LocalDateTime;
import java.util.List;
@Configuration
public class JacksonConfiguration {

	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder.build();
	}

	@Bean
	public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder builder, List<JsonSubTypesProvider> jsonSubTypesProviders) {
		// Create a new Jackson module for custom serialization/deserialization of ZonedDateTime objects
		SimpleModule zonedDateModule = new SimpleModule();
		zonedDateModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializerConverter());
		zonedDateModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializerConverter());

		SimpleModule stringModule = new SimpleModule();
		stringModule.addDeserializer(String.class, new CustomStringDeserializer());

		ParameterNamesModule parameterNamesModule = new ParameterNamesModule();

		BigDecimalModule bigDecimalModule = new BigDecimalModule();

		ObjectMapper objectMapper = builder.modulesToInstall(stringModule, zonedDateModule, parameterNamesModule, bigDecimalModule).build();

		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);

		jsonSubTypesProviders.forEach(jsonSubTypesProvider -> objectMapper
				.registerSubtypes(jsonSubTypesProvider.getTypes().toArray(NamedType[]::new)));

		//set hypersistence hibernate type's object mapper
		ObjectMapperWrapper.INSTANCE.setObjectMapper(objectMapper);

		return new MappingJackson2HttpMessageConverter(objectMapper);
	}

}