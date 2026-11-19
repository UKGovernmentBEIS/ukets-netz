package uk.gov.netz.api.user.core.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * The Keycloak client rest points enum.
 */
@Getter
@AllArgsConstructor
public enum PasswordPolicyErrorCodeEnum {

    INVALID_MIN_LENGTH(Set.of("invalidPasswordMinLengthMessage")),
    INVALID_MAX_LENGTH(Set.of("invalidPasswordMaxLengthMessage")),
    WEAK(Set.of("invalidPasswordStrengthMessage", "invalidPasswordPwnedMessage")),
    PWNED_SERVICE_UNAVAILABLE(Set.of("invalidPasswordPwnedServiceUnavailableMessage")),
    OTHER(Set.of());

    private final Set<String> keycloakErrors;


    @JsonCreator
    public static PasswordPolicyErrorCodeEnum fromValue(String value) {
        return Arrays.stream(values())
            .filter(e -> e.keycloakErrors.contains(value))
            .findFirst()
            .orElse(OTHER);
    }

}
