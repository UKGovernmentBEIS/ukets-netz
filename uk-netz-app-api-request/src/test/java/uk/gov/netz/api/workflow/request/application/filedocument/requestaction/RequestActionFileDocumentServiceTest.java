package uk.gov.netz.api.workflow.request.application.filedocument.requestaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.service.storage.FileDocumentStorageService;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.repository.RequestActionRepository;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmittedRequestActionPayload;

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
    private FileDocumentStorageService fileDocumentStorageService;
    
    @Test
    void generateGetFileDocumentToken(){
    	Long requestActionId = 1L;
        UUID documentUuid = UUID.randomUUID();
        RfiSubmittedRequestActionPayload payload = RfiSubmittedRequestActionPayload
            .builder()
            .officialDocument(FileInfoDTO.builder().uuid(documentUuid.toString()).name("name").build())
            .build();
        RequestAction requestAction = RequestAction.builder().id(requestActionId).payload(payload).build();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.of(requestAction));
        
        FileToken expectedResult = FileToken.builder().build();
        
        when(fileDocumentStorageService.generateGetFileDocumentToken(documentUuid.toString())).thenReturn(expectedResult);

        FileToken result = service.generateGetFileDocumentToken(requestActionId, documentUuid);
        assertThat(result).isEqualTo(expectedResult);

        verify(requestActionRepository, times(1)).findById(requestActionId);
        verify(fileDocumentStorageService, times(1)).generateGetFileDocumentToken(documentUuid.toString());
    }


    @Test
    void generateGetFileDocumentToken_request_action_not_exists(){
        Long requestActionId = 1L;
        UUID fileDocumentUuid = UUID.randomUUID();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.empty());

        BusinessException businessException = assertThrows(BusinessException.class, () ->
        service.generateGetFileDocumentToken(requestActionId, fileDocumentUuid));

        assertThat(businessException.getErrorCode()).isEqualTo(RESOURCE_NOT_FOUND);

        verify(requestActionRepository, times(1)).findById(requestActionId);
        verifyNoInteractions(fileDocumentStorageService);
    }
}
