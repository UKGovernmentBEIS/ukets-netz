package uk.gov.netz.api.account.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountInfoDTO;
import uk.gov.netz.api.common.config.MapperConfig;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface AccountMapper {

    AccountInfoDTO toAccountInfoDTO(Account account);
}
