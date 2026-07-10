package uk.gov.netz.api.terms;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface TermsMapper {

    TermsDTO transformToTermsDTO(Terms terms);

}
