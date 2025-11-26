package uk.gov.netz.api.account.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.common.domain.EmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AccountDTO {

    private Long id;

    @NotBlank
    @Size(max = 255)
    private String name;

    private EmissionTradingScheme emissionTradingScheme;

    private CompetentAuthorityEnum competentAuthority;

    private LocalDateTime acceptedDate;
}
