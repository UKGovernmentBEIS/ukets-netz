package uk.gov.netz.api.workflow.request.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestTaskActionTypeRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestTaskActionTypeRepository cut;
    
    @Test
    void findAllByBlockedByPayment() {
    	RequestTaskActionType requestTaskActionType1 = createRequestTaskActionType("code1", true);
    	createRequestTaskActionType("code2", false);

        flushAndClear();
        
        Set<RequestTaskActionType> result = cut.findAllByBlockedByPayment(true);
        
        assertThat(result).extracting(RequestTaskActionType::getId).containsAnyOf(requestTaskActionType1.getId());
    }
    
}
