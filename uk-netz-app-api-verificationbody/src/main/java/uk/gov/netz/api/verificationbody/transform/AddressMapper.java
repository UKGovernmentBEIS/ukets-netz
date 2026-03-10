package uk.gov.netz.api.verificationbody.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.verificationbody.domain.Address;
import uk.gov.netz.api.verificationbody.domain.dto.AddressDTO;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface AddressMapper {

    Address toAddress(AddressDTO addressDto);
}
