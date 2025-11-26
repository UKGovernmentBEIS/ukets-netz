package uk.gov.netz.api.thirdpartydataprovider.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvider;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientCreateResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderSaveDTO;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDataProviderMapperTest {
	
	private final ThirdPartyDataProviderMapper cut = Mappers.getMapper(ThirdPartyDataProviderMapper.class);
	
	@Test
	void mapToEntity() {
		ThirdPartyDataProviderSaveDTO dto = ThirdPartyDataProviderSaveDTO.builder()
				.clientId("clientId")
				.name("clientname")
				.clientEntityId("clientEntityId")
				.build();
		
		ThirdPartyDataProvider result = cut.mapToEntity(dto);
		
		assertThat(result).isEqualTo(ThirdPartyDataProvider.builder()
				.clientId("clientId")
				.clientEntityId("clientEntityId")
				.name("clientname")
				.build());
	}

	@Test
	void mapToDTO() {
		ThirdPartyDataProvider entity = ThirdPartyDataProvider.builder()
			.clientId("clientId")
			.name("clientname")
			.build();

		ThirdPartyDataProviderDTO result = cut.mapToDTO(entity);

		assertThat(result).isEqualTo(ThirdPartyDataProviderDTO.builder()
			.clientId("clientId")
			.name("clientname")
			.build());
	}
	
	@Test
	void update() {
		ThirdPartyDataProvider entity = ThirdPartyDataProvider.builder()
				.id(1L)
				.clientId("clientId")
				.name("name")
				.build();
		
		ThirdPartyDataProviderDTO dto = ThirdPartyDataProviderDTO.builder()
				.clientId("clientId2")
				.name("name2")
				.build();
		
		cut.update(entity, dto);
		
		assertThat(entity).isEqualTo(ThirdPartyDataProvider.builder()
				.id(1L)
				.clientId("clientId2")
				.name("name2")
				.build());
	}

	@Test
	void mapToDTO_from_create_DTO() {
		ThirdPartyDataProviderClientCreateResponseDTO createDTO = ThirdPartyDataProviderClientCreateResponseDTO.builder()
			.name("clientname")
			.clientSecret("clientSecret")
			.serviceAccountUserId("serviceAccountUserId")
			.clientId("clientId")
			.build();

		ThirdPartyDataProviderSaveDTO result = cut.mapToDTO(createDTO);

		assertThat(result).isEqualTo(ThirdPartyDataProviderSaveDTO.builder()
			.name("clientname")
			.clientId("clientId")
			.build());
	}

	@Test
	void toThirdPartyDataProviderNameInfoDTO() {
		ThirdPartyDataProvider entity = ThirdPartyDataProvider.builder()
			.name("clientname")
			.clientId("clientId")
			.id(1L)
			.createdDate(LocalDateTime.now())
			.build();

		ThirdPartyDataProviderNameInfoDTO result = cut.toThirdPartyDataProviderNameInfoDTO(entity);

		assertThat(result).isEqualTo(ThirdPartyDataProviderNameInfoDTO.builder()
			.name("clientname")
			.id(1L)
			.build());
	}

}
