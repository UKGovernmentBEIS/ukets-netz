package uk.gov.netz.api.workflow.request.application.filedocument.requestaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.files.documents.service.FileDocumentTokenService;
import uk.gov.netz.api.workflow.request.core.repository.RequestActionRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class RequestActionFileDocumentServiceTest {

    @InjectMocks
    private RequestActionFileDocumentService service;

    @Mock
    private RequestActionRepository requestActionRepository;

    @Mock
    private FileDocumentTokenService fileDocumentTokenService;


    @Test
    void generateGetFileDocumentToken_request_action_not_exists(){
        Long requestActionId = 1L;
        UUID fileDocumentUuid = UUID.randomUUID();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.empty());

        BusinessException businessException = assertThrows(BusinessException.class, () ->
        service.generateGetFileDocumentToken(requestActionId, fileDocumentUuid));

        assertThat(businessException.getErrorCode()).isEqualTo(RESOURCE_NOT_FOUND);

        verify(requestActionRepository, times(1)).findById(requestActionId);
        verifyNoInteractions(fileDocumentTokenService);
    }
}
