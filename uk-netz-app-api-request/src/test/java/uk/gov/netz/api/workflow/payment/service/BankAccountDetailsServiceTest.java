package uk.gov.netz.api.workflow.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.payment.domain.BankAccountDetails;
import uk.gov.netz.api.workflow.payment.domain.dto.BankAccountDetailsDTO;
import uk.gov.netz.api.workflow.payment.repository.BankAccountDetailsRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountDetailsServiceTest {

    @InjectMocks
    private BankAccountDetailsService bankAccountDetailsService;

    @Mock
    private BankAccountDetailsRepository bankAccountDetailsRepository;

    @Test
    void getBankAccountDetailsByCa() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        BankAccountDetails bankAccountDetails = BankAccountDetails.builder()
            .competentAuthority(competentAuthority)
            .accountName("accountName")
            .sortCode("sortCode")
            .accountNumber("accountNumber")
            .iban("iban")
            .swiftCode("swiftCode")
            .build();

        when(bankAccountDetailsRepository.findByCompetentAuthority(competentAuthority)).thenReturn(Optional.of(bankAccountDetails));

        BankAccountDetailsDTO result = bankAccountDetailsService.getBankAccountDetailsByCa(competentAuthority);

        assertEquals(bankAccountDetails.getAccountName(), result.getAccountName());
        assertEquals(bankAccountDetails.getAccountNumber(), result.getAccountNumber());
        assertEquals(bankAccountDetails.getSortCode(), result.getSortCode());
        assertEquals(bankAccountDetails.getSwiftCode(), result.getSwiftCode());
        assertEquals(bankAccountDetails.getIban(), result.getIban());
    }

    @Test
    void getBankAccountDetailsByCa_resource_not_found() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        BankAccountDetails bankAccountDetails = BankAccountDetails.builder()
            .competentAuthority(competentAuthority)
            .accountName("accountName")
            .sortCode("sortCode")
            .accountNumber("accountNumber")
            .iban("iban")
            .swiftCode("swiftCode")
            .build();

        when(bankAccountDetailsRepository.findByCompetentAuthority(competentAuthority)).thenReturn(Optional.of(bankAccountDetails));

        BankAccountDetailsDTO result = bankAccountDetailsService.getBankAccountDetailsByCa(competentAuthority);

        assertEquals(bankAccountDetails.getAccountName(), result.getAccountName());
        assertEquals(bankAccountDetails.getAccountNumber(), result.getAccountNumber());
        assertEquals(bankAccountDetails.getSortCode(), result.getSortCode());
        assertEquals(bankAccountDetails.getSwiftCode(), result.getSwiftCode());
        assertEquals(bankAccountDetails.getIban(), result.getIban());
    }
}