package uk.gov.netz.api.authorization.regulator.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class RegulatorPermissionGroupLevel {

    private final String group;

    private final RegulatorPermissionLevel level;
}
