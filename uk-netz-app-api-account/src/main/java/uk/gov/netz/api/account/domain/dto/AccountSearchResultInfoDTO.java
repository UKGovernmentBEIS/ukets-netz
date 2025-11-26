package uk.gov.netz.api.account.domain.dto;


import lombok.Getter;
import lombok.EqualsAndHashCode;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;

@Getter
@EqualsAndHashCode
public class AccountSearchResultInfoDTO {

    private Long id;

    private String name;

    private String businessId;

    private String status;

    public AccountSearchResultInfoDTO(Long id, String name, String businessId, AccountStatus status) {
        this.id = id;
        this.name = name;
        this.businessId = businessId;
        this.status = status != null ? status.getName() : null;
    }
}
