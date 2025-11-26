package uk.gov.netz.api.authorization.rules.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizationRuleScopePermission {
    private String resourceType;
    private String resourceSubType;
    private String handler;
    private String permission;
}
