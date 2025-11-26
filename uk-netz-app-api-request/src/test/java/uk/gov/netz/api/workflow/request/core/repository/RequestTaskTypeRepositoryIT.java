package uk.gov.netz.api.workflow.request.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestTaskTypeRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestTaskTypeRepository cut;
    
    @Test
    void findByCode() {
    	String code = "code";
    	Optional<RequestTaskType> result = cut.findByCode(code);
    	assertThat(result).isEmpty();
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	RequestTaskType requestTaskType = createRequestTaskType(code, requestType, true, "expkey", true, true);

        flushAndClear();
        
        result = cut.findByCode(code);
        
        assertThat(result.get().getId()).isEqualTo(requestTaskType.getId());
    }
    
    @Test
    void findAllByCodeEndingWith() {
    	String code = "WAIT_FOR_RFI_RESPONSE";
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	RequestTaskType requestTaskType1 = createRequestTaskType("FLOW1"  + code, requestType, true, "expkey1", true, true);
    	RequestTaskType requestTaskType2 = createRequestTaskType("FLOW2_"  + code, requestType, true, "expkey2", true, true);
    	createRequestTaskType("FLOW3", requestType, true, "expkey3", true, true);
    	createRequestTaskType(code + "FLOW4", requestType, true, "expkey4", true, true);

        flushAndClear();
        
        Set<RequestTaskType> result = cut.findAllByCodeEndingWith(code);
        
		assertThat(result).extracting(RequestTaskType::getId).containsExactlyInAnyOrder(requestTaskType1.getId(),
				requestTaskType2.getId());
    }
    
    @Test
    void findAllByCodeEndingWithOrCodeEndingWith() {
    	String code1 = "code1";
    	String code2 = "code2";
    	String code3 = "code3";
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	RequestTaskType requestTaskType1 = createRequestTaskType("FLOW1"  + code1, requestType, true, "expkey1", true, true);
    	RequestTaskType requestTaskType2 = createRequestTaskType("FLOW2_"  + code2, requestType, true, "expkey2", true, true);
    	createRequestTaskType("FLOW3_"  + code3, requestType, true, "expkey3", true, true);
    	createRequestTaskType("FLOW4", requestType, true, "expkey4", true, true);
    	createRequestTaskType(code1 + "FLOW5", requestType, true, "expkey5", true, true);

        flushAndClear();
        
        Set<RequestTaskType> result = cut.findAllByCodeEndingWithOrCodeEndingWith(code1, code2);
        
		assertThat(result).extracting(RequestTaskType::getId).containsExactlyInAnyOrder(requestTaskType1.getId(),
				requestTaskType2.getId());
    }
    
}
