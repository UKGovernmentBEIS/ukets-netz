package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequestExpirationVarsBuilder {
    
    private final RequestCalculateExpirationService requestCalculateExpirationService;
    
    public Map<String, Object> buildExpirationVars(String requestExpirationKey) {
        final Date requestExpirationDate = requestCalculateExpirationService.calculateExpirationDate();
        return this.buildExpirationVars(requestExpirationKey, requestExpirationDate);
    }
    
    public Map<String, Object> buildExpirationVars(String requestExpirationKey, Date expirationDate) {
        Map<String, Object> expirationVars = new HashMap<>();
        expirationVars.put(requestExpirationKey + BpmnProcessConstants._EXPIRATION_DATE, expirationDate);
        expirationVars.put(requestExpirationKey + BpmnProcessConstants._FIRST_REMINDER_DATE, 
                requestCalculateExpirationService.calculateFirstReminderDate(expirationDate));
        expirationVars.put(requestExpirationKey + BpmnProcessConstants._SECOND_REMINDER_DATE, 
                requestCalculateExpirationService.calculateSecondReminderDate(expirationDate));
        return expirationVars;
    }

}
