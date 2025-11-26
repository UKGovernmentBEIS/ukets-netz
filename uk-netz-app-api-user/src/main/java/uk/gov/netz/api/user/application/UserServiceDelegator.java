package uk.gov.netz.api.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;
import uk.gov.netz.api.user.core.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceDelegator {

    private final UserRoleTypeService userRoleTypeService;
    private final UserService userService;
    private final List<UserRoleTypeAuthService> userRoleTypeAuthServiceList;

    public UserDTO getUserById(String userId) {
    	final Optional<UserRoleTypeDTO> userRoleTypeOpt = userRoleTypeService.getUserRoleTypeByUserIdOpt(userId);
		return userRoleTypeOpt
				.map(userRoleType -> userRoleTypeAuthServiceList.stream()
						.filter(service -> service.getRoleType().equals(userRoleType.getRoleType()))
						.findAny()
						.map(service -> service.getUserById(userId))
						.orElse(null))
				.orElseGet(() -> userService.getUserByUserId(userId));
    }
    
    public UserDTO getCurrentUserDTO(AppUser currentUser) {
    	final String userId = currentUser.getUserId();
    	final Optional<UserRoleTypeDTO> userRoleTypeOpt = userRoleTypeService.getUserRoleTypeByUserIdOpt(userId);
		return userRoleTypeOpt
				.map(userRoleType -> userRoleTypeAuthServiceList.stream()
						.filter(service -> service.getRoleType().equals(userRoleType.getRoleType()))
						.findAny()
						.map(service -> service.getCurrentUserDTO(currentUser))
						.orElse(null))
				.orElseGet(() -> userService.getUserByUserId(userId));
    }
}