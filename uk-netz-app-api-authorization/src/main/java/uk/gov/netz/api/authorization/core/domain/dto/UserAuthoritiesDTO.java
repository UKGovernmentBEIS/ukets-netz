package uk.gov.netz.api.authorization.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuthoritiesDTO {

    @Builder.Default
    private List<UserAuthorityDTO> authorities = new ArrayList<>();

    /**
     * Whether the user authority properties should be considered as editable
     */
    private boolean editable;
}
