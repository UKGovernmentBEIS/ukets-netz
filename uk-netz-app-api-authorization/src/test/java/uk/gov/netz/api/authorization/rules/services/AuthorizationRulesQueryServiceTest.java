package uk.gov.netz.api.authorization.rules.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationRulesQueryServiceTest {
    
    @InjectMocks
    private AuthorizationRulesQueryService service;
    
    @Mock
    private AuthorizationRuleRepository authorizationRuleRepository;
    
    @Test
    void findByResourceTypeAndResourceSubTypeAndScope() {
        String resourceType = ResourceType.REQUEST_TASK;
        String resourceSubType = "requestTaskType";
        
        when(authorizationRuleRepository.findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType))
            .thenReturn(Optional.of(RoleTypeConstants.OPERATOR));
        
        Optional<String> result = service.findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType);
        
        assertThat(result)
                .isNotEmpty()
                .contains(RoleTypeConstants.OPERATOR);
        verify(authorizationRuleRepository, times(1)).findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType);
    }
    
    @Test
    void findByResourceTypeAndResourceSubTypeAndScope_not_found() {
        String resourceType = ResourceType.REQUEST_TASK;
        String resourceSubType = "requestTaskType";
        
        when(authorizationRuleRepository.findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType))
            .thenReturn(Optional.empty());
        
        Optional<String> result = service.findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType);
        
        assertThat(result).isEmpty();
        verify(authorizationRuleRepository, times(1)).findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType);
    }
}
