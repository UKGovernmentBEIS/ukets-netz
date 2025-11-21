package uk.gov.netz.api.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestEmissionTradingScheme implements EmissionTradingScheme {
	
    DUMMY_EMISSION_TRADING_SCHEME("DUMMY"),
    DUMMY_EMISSION_TRADING_SCHEME_2("DUMMY2")
    ;

    private final String description;

    @Override
    public String getName() {
        return this.name();
    }

}
