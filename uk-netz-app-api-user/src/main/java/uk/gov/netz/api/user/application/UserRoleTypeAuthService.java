package uk.gov.netz.api.user.application;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;

public interface UserRoleTypeAuthService<T extends UserDTO> {
	
    T getUserById(String id);
    
    T getCurrentUserDTO(AppUser currentUser);
    
    String getRoleType();
    
}