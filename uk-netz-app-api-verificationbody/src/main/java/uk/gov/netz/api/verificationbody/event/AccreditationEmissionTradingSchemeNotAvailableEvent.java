package uk.gov.netz.api.verificationbody.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class AccreditationEmissionTradingSchemeNotAvailableEvent {

    private final Long verificationBodyId;
    private final Set<String> notAvailableAccreditationEmissionTradingSchemes;
}
