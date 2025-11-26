package uk.gov.netz.api.authorization.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.UserRoleType;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.repository.UserRoleTypeRepository;
import uk.gov.netz.api.authorization.core.transform.UserRoleTypeMapper;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserRoleTypeServiceTest {

    @InjectMocks
    private UserRoleTypeService cut;

    @Mock
    private UserRoleTypeRepository userRoleTypeRepository;
    
    @Mock
    private UserRoleTypeMapper userRoleTypeMapper;

    @Test
    void getRoleTypeByUserId() {
    	String userId = "userId";
        UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build();

        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);
        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));

        UserRoleTypeDTO result = cut.getUserRoleTypeByUserId(userId);

        verify(userRoleTypeRepository, times(1)).findById(userId);
        verify(userRoleTypeMapper, times(1)).toUserRoleTypeDTO(userRoleType);

        assertEquals(userRoleTypeDTO, result);
    }

    @Test
    void getRoleTypeByUserId_user_role_not_found() {
    	String userId = "userId";

        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () -> cut.getUserRoleTypeByUserId(userId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.USER_ROLE_NOT_FOUND);

        verify(userRoleTypeRepository, times(1)).findById(userId);
        verifyNoInteractions(userRoleTypeMapper);
    }
    
    @Test
    void createUserRoleTypeIfNotExist_not_exist_should_save() {
    	String userId = "userId";
    	String roleType = RoleTypeConstants.OPERATOR;
    	
    	when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.empty());
    	
    	cut.createUserRoleTypeIfNotExist(userId, roleType);
    	
    	verify(userRoleTypeRepository, times(1)).findById(userId);
    	verify(userRoleTypeRepository, times(1)).save(UserRoleType.builder()
    			.userId(userId)
    			.roleType(roleType)
    			.build());
    	verifyNoInteractions(userRoleTypeMapper);
    }
    
    @Test
    void createUserRoleTypeIfNotExist_exist_should_not_save() {
    	String userId = "userId";
    	String roleType = RoleTypeConstants.OPERATOR;
    	
    	UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(roleType).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(roleType).build();

        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);
        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
    	
    	cut.createUserRoleTypeIfNotExist(userId, roleType);
    	
    	verify(userRoleTypeRepository, times(1)).findById(userId);
    	verify(userRoleTypeMapper, times(1)).toUserRoleTypeDTO(userRoleType);
    	verify(userRoleTypeRepository, never()).save(Mockito.any(UserRoleType.class));
    }
    
    @Test
    void deleteUserRoleType() {
    	String userId = "userId";
    	
    	cut.deleteUserRoleType(userId);
    	
    	verify(userRoleTypeRepository, times(1)).deleteById(userId);
    }
    
    @Test
    void validateUserRoleTypeIsEmpty_not_empty() {
    	String userId = "userId";
    	String roleType = RoleTypeConstants.OPERATOR;
    	
    	UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(roleType).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(roleType).build();
        
        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);
        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
    	
    	BusinessException be = assertThrows(BusinessException.class, () -> cut.validateUserRoleTypeIsEmpty(userId));
    	
    	assertThat(be.getErrorCode()).isEqualTo(ErrorCode.USER_ROLE_ALREADY_EXISTS);
    	
    	verify(userRoleTypeRepository, times(1)).findById(userId);
    	verify(userRoleTypeMapper, times(1)).toUserRoleTypeDTO(userRoleType);
    }
    
    @Test
    void validateUserRoleTypeIsEmpty_empty() {
    	String userId = "userId";
    	
    	when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.empty());
    	
    	cut.validateUserRoleTypeIsEmpty(userId);
    	
    	verify(userRoleTypeRepository, times(1)).findById(userId);
    	verifyNoInteractions(userRoleTypeMapper);
    }
    
    @Test
    void validateUserRoleTypeIsOfTypeOrNotExist_different_role_type() {
    	String userId = "userId";
    	String roleType = RoleTypeConstants.OPERATOR;
    	
    	UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();
        
        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);
        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
        
        BusinessException be = assertThrows(BusinessException.class, () -> cut.validateUserRoleTypeIsOfTypeOrNotExist(userId, roleType));
    	assertThat(be.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_REGISTERED_WITH_DIFFERENT_ROLE);
        
        verify(userRoleTypeRepository, times(1)).findById(userId);
    	verify(userRoleTypeMapper, times(1)).toUserRoleTypeDTO(userRoleType);
    }
    
    @Test
    void validateUserRoleTypeIsOfTypeOrNotExist_same_role_type() {
    	String userId = "userId";
    	String roleType = RoleTypeConstants.OPERATOR;
    	
    	UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(roleType).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(roleType).build();
        
        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);
        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
        
        cut.validateUserRoleTypeIsOfTypeOrNotExist(userId, roleType);
        
        verify(userRoleTypeRepository, times(1)).findById(userId);
    	verify(userRoleTypeMapper, times(1)).toUserRoleTypeDTO(userRoleType);
    }

    @Test
    void isUserOperator() {
    	String userId = "userId";
        UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build();

        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);

        assertTrue(cut.isUserOperator(userId));
    }

    @Test
    void isUserRegulator() {
    	String userId = "userId";
        UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();

        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);

        assertTrue(cut.isUserRegulator(userId));
    }

    @Test
    void isUserVerifier() {
    	String userId = "userId";
        UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(RoleTypeConstants.VERIFIER).build();
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.VERIFIER).build();

        when(userRoleTypeRepository.findById(userId)).thenReturn(Optional.of(userRoleType));
        when(userRoleTypeMapper.toUserRoleTypeDTO(userRoleType)).thenReturn(userRoleTypeDTO);

        assertTrue(cut.isUserVerifier(userId));
    }

}