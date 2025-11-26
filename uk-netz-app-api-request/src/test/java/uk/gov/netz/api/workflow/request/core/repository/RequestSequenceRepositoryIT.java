package uk.gov.netz.api.workflow.request.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestSequenceRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestSequenceRepository cut;
    
    @Test
    void findByType() {
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	
    	RequestSequence requestSequence = new RequestSequence(requestType);
    	entityManager.persist(requestSequence);
    	
        flushAndClear();
        
        Optional<RequestSequence> result = cut.findByRequestType(requestType);
        assertThat(result).isNotEmpty();
        assertThat(result.get().getRequestType().getCode()).isEqualTo("DUMMY_REQUEST_TYPE");
    }
    
    @Test
    void findByBusinessIdentifierAndType() {
    	String businessIdentifier = "bi";
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	
    	flushAndClear();
    	
    	Optional<RequestSequence> result = cut.findByBusinessIdentifierAndRequestType(businessIdentifier, requestType);
        assertThat(result).isEmpty();
        
    	RequestSequence requestSequence = new RequestSequence(businessIdentifier, requestType);
    	entityManager.persist(requestSequence);
    	
        flushAndClear();
        
        result = cut.findByBusinessIdentifierAndRequestType(businessIdentifier, requestType);
        assertThat(result).isNotEmpty();
        assertThat(result.get().getRequestType().getCode()).isEqualTo("DUMMY_REQUEST_TYPE");
        
    }
    
}