package uk.gov.netz.api.request.feedback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.repository.RequestFeedbackRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestFeedbackQueryServiceTest {

    @InjectMocks
    private RequestFeedbackQueryService queryService;

    @Mock
    private RequestFeedbackRepository repository;

    @Test
    void getRequestFeedback() {
        String type = "type";
        String requestId = "requestId";
        RequestFeedback expected = mock(RequestFeedback.class);

        when(repository.findByRequestIdAndType(requestId, type)).thenReturn(Optional.of(expected));

        RequestFeedback requestFeedback = queryService.getRequestFeedback(requestId, type);

        assertThat(requestFeedback).isEqualTo(expected);

        verify(repository).findByRequestIdAndType(requestId, type);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getRequestFeedback_throws_error() {
        String type = "type";
        String requestId = "requestId";

        when(repository.findByRequestIdAndType(requestId, type)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () -> queryService.getRequestFeedback(requestId, type));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(repository).findByRequestIdAndType(requestId, type);
        verifyNoMoreInteractions(repository);
    }

}