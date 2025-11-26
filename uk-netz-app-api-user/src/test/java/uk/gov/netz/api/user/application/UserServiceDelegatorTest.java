package uk.gov.netz.api.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;
import uk.gov.netz.api.user.core.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceDelegatorTest {

    private UserServiceDelegator cut;

    private UserRoleTypeService userRoleTypeService;
    private UserService userService;
    private UserRoleTypeAuthService operatorUserAuthService;
    private UserRoleTypeAuthService regulatorUserAuthService;
    private UserRoleTypeAuthService verifierUserAuthService;
    private UserDTO user;

    @BeforeEach
    void setup() {
    	userService = Mockito.mock(UserService.class);
        operatorUserAuthService = Mockito.mock(UserRoleTypeAuthService.class);
        regulatorUserAuthService = Mockito.mock(UserRoleTypeAuthService.class);
        verifierUserAuthService = Mockito.mock(UserRoleTypeAuthService.class);
        userRoleTypeService = Mockito.mock(UserRoleTypeService.class);

        cut = new UserServiceDelegator(userRoleTypeService,
        		userService,
                List.of(operatorUserAuthService, regulatorUserAuthService, verifierUserAuthService));

        user = UserDTO.builder()
                .email("email")
                .firstName("firstName")
                .lastName("lastName")
                .build();
    }

    @Test
    void getUserById_Operator() {
        final String userId = "userId";
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build();

        when(userRoleTypeService.getUserRoleTypeByUserIdOpt(userId)).thenReturn(Optional.of(userRoleTypeDTO));
        when(operatorUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(operatorUserAuthService.getUserById(userId)).thenReturn(user);

        // Invoke
        cut.getUserById(userId);

        // Assert
        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserIdOpt(userId);
        verify(operatorUserAuthService, times(1)).getUserById(userId);
        verify(regulatorUserAuthService, never()).getUserById(Mockito.anyString());
        verify(verifierUserAuthService, never()).getUserById(Mockito.anyString());
    }

    @Test
    void getUserById_Regulator() {
        final String userId = "userId2";
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();

        when(userRoleTypeService.getUserRoleTypeByUserIdOpt(userId)).thenReturn(Optional.of(userRoleTypeDTO));
        when(operatorUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(regulatorUserAuthService.getUserById(userId)).thenReturn(user);

        // Invoke
        cut.getUserById(userId);

        // Assert
        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserIdOpt(userId);
        verify(operatorUserAuthService, never()).getUserById(Mockito.anyString());
        verify(regulatorUserAuthService, times(1)).getUserById(userId);
        verify(verifierUserAuthService, never()).getUserById(Mockito.anyString());
    }

    @Test
    void getUserById_Verifier() {
        final String userId = "userId3";
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.VERIFIER).build();

        when(userRoleTypeService.getUserRoleTypeByUserIdOpt(userId)).thenReturn(Optional.of(userRoleTypeDTO));
        when(operatorUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(verifierUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.VERIFIER);
        when(verifierUserAuthService.getUserById(userId)).thenReturn(user);

        // Invoke
        cut.getUserById(userId);

        // Assert
        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserIdOpt(userId);
        verify(operatorUserAuthService, never()).getUserById(Mockito.anyString());
        verify(regulatorUserAuthService, never()).getUserById(Mockito.anyString());
        verify(verifierUserAuthService, times(1)).getUserById(userId);
    }
    
    @Test
    void getUserById_no_role_type() {
        final String userId = "userId";

        when(userRoleTypeService.getUserRoleTypeByUserIdOpt(userId)).thenReturn(Optional.empty());
        when(userService.getUserByUserId(userId)).thenReturn(user);

        // Invoke
        UserDTO result = cut.getUserById(userId);

        assertThat(result).isEqualTo(user);
        
        // Assert
        verify(userService, times(1)).getUserByUserId(userId);
        verifyNoInteractions(operatorUserAuthService, regulatorUserAuthService, verifierUserAuthService);
    }
    
    
    @Test
    void getCurrentUserDTO_Operator() {
        final String userId = "userId";
        AppUser currentUser = AppUser.builder()
        		.userId(userId)
        		.build();
        
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build();

        when(userRoleTypeService.getUserRoleTypeByUserIdOpt(userId)).thenReturn(Optional.of(userRoleTypeDTO));
        when(operatorUserAuthService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(operatorUserAuthService.getCurrentUserDTO(currentUser)).thenReturn(user);

        // Invoke
        cut.getCurrentUserDTO(currentUser);

        // Assert
        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserIdOpt(userId);
        verify(operatorUserAuthService, times(1)).getCurrentUserDTO(currentUser);
        verify(regulatorUserAuthService, never()).getUserById(Mockito.anyString());
        verify(verifierUserAuthService, never()).getUserById(Mockito.anyString());
    }

    @Test
    void getCurrentUserDTO_no_role_type() {
        final String userId = "userId";
        
        AppUser currentUser = AppUser.builder()
        		.userId(userId)
        		.build();

        when(userRoleTypeService.getUserRoleTypeByUserIdOpt(userId)).thenReturn(Optional.empty());
        when(userService.getUserByUserId(userId)).thenReturn(user);

        // Invoke
        UserDTO result = cut.getCurrentUserDTO(currentUser);

        assertThat(result).isEqualTo(user);
        
        // Assert
        verify(userService, times(1)).getUserByUserId(userId);
        verifyNoInteractions(operatorUserAuthService, regulatorUserAuthService, verifierUserAuthService);
    }
}