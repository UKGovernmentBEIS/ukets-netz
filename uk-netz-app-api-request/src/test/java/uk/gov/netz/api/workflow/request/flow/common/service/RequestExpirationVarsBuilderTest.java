package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestExpirationVarsBuilderTest {

    @InjectMocks
    private RequestExpirationVarsBuilder builder;

    @Mock
    private RequestCalculateExpirationService requestCalculateExpirationService;
    
    @Test
    void buildExpirationVars_by_requestType_and_requestExpirationType() {
        String requestExpirationType = "PAYMENT";
        
        Date expirationDate = new Date();
        Date firstReminderDate = new Date();
        Date secondReminderDate = new Date();
        
        when(requestCalculateExpirationService.calculateExpirationDate()).thenReturn(expirationDate);
        when(requestCalculateExpirationService.calculateFirstReminderDate(expirationDate)).thenReturn(firstReminderDate);
        when(requestCalculateExpirationService.calculateSecondReminderDate(expirationDate)).thenReturn(secondReminderDate);
        
        // invoke
        Map<String, Object> result = builder.buildExpirationVars(requestExpirationType);
        
        assertThat(result).isEqualTo(Map.of(
        		requestExpirationType + BpmnProcessConstants._EXPIRATION_DATE, expirationDate,
        		requestExpirationType + BpmnProcessConstants._FIRST_REMINDER_DATE, firstReminderDate, 
        		requestExpirationType + BpmnProcessConstants._SECOND_REMINDER_DATE, secondReminderDate)
                );
        verify(requestCalculateExpirationService, times(1)).calculateExpirationDate();
        verify(requestCalculateExpirationService, times(1)).calculateFirstReminderDate(expirationDate);
        verify(requestCalculateExpirationService, times(1)).calculateSecondReminderDate(expirationDate);
    }
    
    @Test
    void buildExpirationrVars_by_requestExpirationType_and_expirationDate() {
        String requestExpirationType = "APPLICATION_REVIEW";
        
        Date expirationDate = new Date();
        Date firstReminderDate = new Date();
        Date secondReminderDate = new Date();
        
        when(requestCalculateExpirationService.calculateFirstReminderDate(expirationDate)).thenReturn(firstReminderDate);
        when(requestCalculateExpirationService.calculateSecondReminderDate(expirationDate)).thenReturn(secondReminderDate);
        
        // invoke
        Map<String, Object> result = builder.buildExpirationVars(requestExpirationType, expirationDate);
        
        assertThat(result).isEqualTo(Map.of(
        		requestExpirationType + BpmnProcessConstants._EXPIRATION_DATE, expirationDate,
        		requestExpirationType + BpmnProcessConstants._FIRST_REMINDER_DATE, firstReminderDate, 
        		requestExpirationType + BpmnProcessConstants._SECOND_REMINDER_DATE, secondReminderDate)
                );
        verify(requestCalculateExpirationService, times(1)).calculateFirstReminderDate(expirationDate);
        verify(requestCalculateExpirationService, times(1)).calculateSecondReminderDate(expirationDate);
    }
}
