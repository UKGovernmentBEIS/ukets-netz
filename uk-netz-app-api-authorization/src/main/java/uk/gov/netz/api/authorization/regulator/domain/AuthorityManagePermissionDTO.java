package uk.gov.netz.api.authorization.regulator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AuthorityManagePermissionDTO {

    private Map<String, RegulatorPermissionLevel> permissions;

    private boolean editable;
}
