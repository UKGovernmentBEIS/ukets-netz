package uk.gov.netz.api.workflow.request.core.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RfiRdeTaskExistenceRequestTaskActionValidatorTest {
	
	@InjectMocks
    private RfiRdeTaskExistenceRequestTaskActionValidator cut;
	
	@Mock
	private RequestTaskTypeRepository requestTaskTypeRepository;

    @Test
    void getTypes() {
        assertThat(cut.getTypes()).isEqualTo(Set.of(RequestTaskActionTypes.RFI_SUBMIT, RequestTaskActionTypes.RDE_SUBMIT)
        );
    }

    @Test
    void getConflictingRequestTaskTypes() {
		when(requestTaskTypeRepository.findAllByCodeEndingWithOrCodeEndingWith(RequestTaskTypes.WAIT_FOR_RFI_RESPONSE,
				RequestTaskTypes.WAIT_FOR_RDE_RESPONSE))
				.thenReturn(Set.of(RequestTaskType.builder().code("code1").build(),
						RequestTaskType.builder().code("code2").build()));
    	
    	Set<String> result = cut.getConflictingRequestTaskTypes();
    	
    	assertThat(result).containsExactlyInAnyOrder("code1", "code2");
    	
		verify(requestTaskTypeRepository, times(1)).findAllByCodeEndingWithOrCodeEndingWith(
				RequestTaskTypes.WAIT_FOR_RFI_RESPONSE, RequestTaskTypes.WAIT_FOR_RDE_RESPONSE);
    }
}
