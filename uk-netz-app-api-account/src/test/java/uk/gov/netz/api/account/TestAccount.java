package uk.gov.netz.api.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity(name = "test_account")
@Table(name = "test_account")
public class TestAccount extends Account {
	
	@Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
	private TestAccountStatus status;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "emission_trading_scheme")
    @NotNull
	private TestEmissionTradingScheme emissionTradingScheme;

}
