package uk.gov.netz.api.userinfoapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class UserInfoDTO {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    
    /**
	 * Annotated with JsonIgnore because enabled should never served/posted to/by the web layer.
	 */
    @JsonIgnore
    private boolean enabled;
    
    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
