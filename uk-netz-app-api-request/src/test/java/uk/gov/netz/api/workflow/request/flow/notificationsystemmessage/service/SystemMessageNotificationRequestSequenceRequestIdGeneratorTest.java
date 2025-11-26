package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestSequenceRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemMessageNotificationRequestSequenceRequestIdGeneratorTest {

    @InjectMocks
    private SystemMessageNotificationRequestSequenceRequestIdGenerator cut;

    @Mock
    private RequestSequenceRepository requestSequenceRepository;

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @Test
    void getTypes() {
        assertThat(cut.getTypes()).containsExactly(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION);
    }

    @Test
    void getPrefix() {
        assertThat(cut.getPrefix()).isNull();
    }

    @Test
    void generate() {
        long currentSequence = 3;
        RequestParams params = RequestParams.builder()
            .type(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION)
            .build();

        RequestSequence requestSequence = RequestSequence.builder()
            .id(2L)
            .sequence(currentSequence)
            .build();

        RequestType requestType = Mockito.mock(RequestType.class);

        when(requestTypeRepository.findByCode(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION))
            .thenReturn(Optional.of(requestType));

        when(requestSequenceRepository.findByRequestType(requestType))
            .thenReturn(Optional.of(requestSequence));

        //invoke
        String result = cut.generate(params);

        assertThat(result).isEqualTo(String.valueOf(currentSequence + 1));
        verify(requestSequenceRepository, times(1)).findByRequestType(requestType);
        ArgumentCaptor<RequestSequence> requestSequenceCaptor = ArgumentCaptor.forClass(RequestSequence.class);
        verify(requestSequenceRepository, times(1)).save(requestSequenceCaptor.capture());
        verify(requestTypeRepository, times(1)).findByCode(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION);
        RequestSequence requestSequenceCaptured = requestSequenceCaptor.getValue();
        assertThat(requestSequenceCaptured.getSequence()).isEqualTo(currentSequence + 1);
    }
}
