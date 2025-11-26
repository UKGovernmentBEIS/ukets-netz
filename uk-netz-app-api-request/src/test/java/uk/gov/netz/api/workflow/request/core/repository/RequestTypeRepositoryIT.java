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
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestTypeRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestTypeRepository cut;
    
    @Test
    void findByCode() {
    	String code = "code";
    	Optional<RequestType> result = cut.findByCode(code);
    	assertThat(result).isEmpty();
    	
		RequestType requestType = createRequestType(code, "Descr", "processdef", "histcat", true, true, true, true, ResourceType.ACCOUNT);

        flushAndClear();
        
        result = cut.findByCode(code);
        
        assertThat(result.get().getId()).isEqualTo(requestType.getId());
    }
    
    @Test
    void findAllByCanCreateManually() {
		RequestType requestType1 = createRequestType("code1", "Descr1", "processdef1", "histcat1", true, true, true, true, ResourceType.ACCOUNT);
		createRequestType("code2", "Descr2", "processdef2", "histcat2", true, true, true, false, ResourceType.ACCOUNT);

        flushAndClear();
        
        Set<RequestType> result = cut.findAllByCanCreateManually(true);
        
        assertThat(result).extracting(RequestType::getId).containsAnyOf(requestType1.getId());
    }
    
    @Test
    void findAllByCanCreateManuallyAndResourceType() {
		RequestType requestType1 = createRequestType("code1", "Descr1", "processdef1", "histcat1", true, true, true, true, ResourceType.ACCOUNT);
		requestType1.setResourceType(ResourceType.ACCOUNT);
		createRequestType("code2", "Descr2", "processdef2", "histcat2", true, true, true, false, ResourceType.ACCOUNT);

        flushAndClear();
        
        Set<RequestType> result = cut.findAllByCanCreateManuallyAndResourceType(true, "ACCOUNT");
        
        assertThat(result).extracting(RequestType::getId).containsAnyOf(requestType1.getId());
    }

}
