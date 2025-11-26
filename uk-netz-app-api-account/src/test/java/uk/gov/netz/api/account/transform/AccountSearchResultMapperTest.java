package uk.gov.netz.api.account.transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountSearchResultMapperTest {

    private AccountSearchResultMapper mapper;

    @BeforeEach
    public void init() {
        mapper = Mappers.getMapper(AccountSearchResultMapper.class);
    }

    @Test
    void toAccountInfoDTO() {
        final TestAccount account = TestAccount.builder()
                .id(1L)
                .name("Test Account")
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .status(TestAccountStatus.DUMMY)
                .businessId("test business id")
                .build();

        // invoke
        final AccountSearchResultInfoDTO accountInfoDTO = mapper.toAccountInfoDTO(account);

        //verify
        assertThat(accountInfoDTO.getId()).isEqualTo(account.getId());
        assertThat(accountInfoDTO.getName()).isEqualTo(account.getName());
        assertThat(accountInfoDTO.getStatus()).isEqualTo(account.getStatus().getName());
        assertThat(accountInfoDTO.getBusinessId()).isEqualTo(account.getBusinessId());

    }

}
