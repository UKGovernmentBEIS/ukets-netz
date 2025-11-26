package uk.gov.netz.api.authorization.core.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.authorization.core.domain.UserRoleType;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.repository.UserRoleTypeRepository;
import uk.gov.netz.api.authorization.core.transform.UserRoleTypeMapper;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserRoleTypeService {

	private final UserRoleTypeRepository userRoleTypeRepository;
	private final UserRoleTypeMapper userRoleTypeMapper;
	
	public Optional<UserRoleTypeDTO> getUserRoleTypeByUserIdOpt(String userId) {
		return userRoleTypeRepository.findById(userId).map(userRoleTypeMapper::toUserRoleTypeDTO);
	}
	
	public UserRoleTypeDTO getUserRoleTypeByUserId(String userId) {
        return getUserRoleTypeByUserIdOpt(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_ROLE_NOT_FOUND));
    }
	
    public boolean existUserRoleType(String userId) {
        return userRoleTypeRepository.existsByUserId(userId);
    }
    
    @Transactional
    public void createUserRoleTypeOrThrowExceptionIfExists(String userId, String roleType) {
    	if(userRoleTypeRepository.existsById(userId)) {
    		throw new BusinessException(ErrorCode.USER_ROLE_ALREADY_EXISTS);
    	} else {
    		userRoleTypeRepository.save(UserRoleType.builder()
        			.userId(userId)
        			.roleType(roleType)
        			.build());
    	}
    }
    
    @Transactional
    public void createUserRoleTypeIfNotExist(String userId, String roleType) {
    	Optional<UserRoleTypeDTO> userRoleTypeOpt = getUserRoleTypeByUserIdOpt(userId);
    	if(userRoleTypeOpt.isEmpty()){
    		userRoleTypeRepository.save(UserRoleType.builder()
        			.userId(userId)
        			.roleType(roleType)
        			.build());
    	} else {
    		if(!userRoleTypeOpt.get().getRoleType().equals(roleType)) {
    			log.error("User '{}' has already been introduced with role other than {}", () -> userId, () -> roleType);
                throw new BusinessException(ErrorCode.USER_ALREADY_REGISTERED_WITH_DIFFERENT_ROLE);
    		}
    	}
    }
    
    @Transactional
    public void deleteUserRoleType(String userId) {
    	userRoleTypeRepository.deleteById(userId);
    }
    
    public void validateUserRoleTypeIsEmpty(String userId) {
    	final Optional<UserRoleTypeDTO> userRoleTypeOpt = getUserRoleTypeByUserIdOpt(userId);
    	if(userRoleTypeOpt.isPresent()) {
    		throw new BusinessException(ErrorCode.USER_ROLE_ALREADY_EXISTS);
    	}
    }
    
    public void validateUserRoleTypeIsOfTypeOrNotExist(String userId, String roleType) {
    	final Optional<UserRoleTypeDTO> userRoleTypeOpt = getUserRoleTypeByUserIdOpt(userId);
    	userRoleTypeOpt.ifPresent(rt -> {
    		if(!rt.getRoleType().equals(roleType)) {
    			log.error("User '{}' has already been introduced with role other than {}", () -> userId, () -> roleType);
                throw new BusinessException(ErrorCode.USER_ALREADY_REGISTERED_WITH_DIFFERENT_ROLE);
    		}
    	});
    }
	
	 /**
     * Checks if the role of the provided user is OPERATOR.
     * @param userId the provided user
     * @return boolean value representing whether the provided user is OPERATOR or not
     */
    public boolean isUserOperator(String userId) {
        return RoleTypeConstants.OPERATOR.equals(getUserRoleTypeByUserId(userId).getRoleType());
    }
    
    public boolean isUserRegulator(String userId) {
        return RoleTypeConstants.REGULATOR.equals(getUserRoleTypeByUserId(userId).getRoleType());
    }

    public boolean isUserVerifier(String userId) {
        return RoleTypeConstants.VERIFIER.equals(getUserRoleTypeByUserId(userId).getRoleType());
    }
}
