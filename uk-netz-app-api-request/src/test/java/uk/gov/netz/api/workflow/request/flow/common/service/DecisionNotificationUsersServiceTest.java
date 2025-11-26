package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.CaExternalContactService;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionNotificationUsersServiceTest {

	@InjectMocks
    private DecisionNotificationUsersService service;

    @Mock
    private UserInfoApi userInfoApi;
    
    @Mock
    private CaExternalContactService caExternalContactService;
    
    @Test
    void findUserEmails() {
    	List<String> operatorUserIds = List.of("operator1", "operator2");
    	DecisionNotification decisionNotification = DecisionNotification.builder()
    			.operators(new LinkedHashSet<>(operatorUserIds))
    			.externalContacts(Set.of(3L, 4L))
    			.build();
    	
    	List<UserInfo> operators = List.of(
    			UserInfo.builder().firstName("fn_operator1").lastName("ln_operator1").email("operator1@email").build(),
    			UserInfo.builder().firstName("fn_operator2").lastName("ln_operator2").email("operator2@email").build()
    			);
    	
    	List<String> externalContactEmails = List.of("external3@email", "external4@email");
    	
    	when(userInfoApi.getUsers(operatorUserIds)).thenReturn(operators);
    	when(caExternalContactService.getCaExternalContactEmailsByIds(Set.of(3L, 4L))).thenReturn(externalContactEmails);
    	
    	List<String> result = service.findUserEmails(decisionNotification);
    	
		assertThat(result).containsExactlyInAnyOrder("operator1@email", "operator2@email", "external3@email",
				"external4@email");
    	
    	verify(userInfoApi, times(1)).getUsers(operatorUserIds);
    	verify(caExternalContactService, times(1)).getCaExternalContactEmailsByIds(Set.of(3L, 4L));
    	
    }
}
