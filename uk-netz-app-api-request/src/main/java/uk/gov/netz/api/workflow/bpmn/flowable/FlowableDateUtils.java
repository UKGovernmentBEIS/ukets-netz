package uk.gov.netz.api.workflow.bpmn.flowable;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FlowableDateUtils {

    public boolean isDateInThePast(Date date) {
        return date.before(new Date());
    }
    
}
