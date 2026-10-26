package uk.gov.netz.api.request.feedback.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.DateService;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackDetailsDTO;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackPayload;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackStatus;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackType;
import uk.gov.netz.api.request.feedback.repository.RequestFeedbackRepository;
import uk.gov.netz.api.request.feedback.transform.RequestFeedbackMapper;
import uk.gov.netz.api.request.feedback.validation.RequestFeedbackValidator;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestFeedbackServiceTest {

    @InjectMocks
    private RequestFeedbackService service;

    @Mock
    private RequestFeedbackQueryService queryService;
    @Mock
    private RequestFeedbackRepository repository;
    @Mock
    private RequestFeedbackMapper mapper;
    @Mock
    private DateService dateService;
    @Mock
    private RequestFeedbackValidator validator;

    @Test
    void createRequestFeedback_disabled() throws NoSuchFieldException, IllegalAccessException {
        String type = "type";
        String requestId = "requestId";
        AppUser appUser = AppUser.builder().build();

        setRequestFeedbackEnabledValue(false);

        service.createRequestFeedback(requestId, type, appUser);

        verifyNoInteractions(queryService, repository, mapper, dateService);
    }

    @Test
    void createRequestFeedback_already_exists() throws NoSuchFieldException, IllegalAccessException {
        String type = "type";
        String requestId = "requestId";
        AppUser appUser = AppUser.builder().build();

        setRequestFeedbackEnabledValue(true);

        when(repository.existsByRequestIdAndType(requestId, type)).thenReturn(true);

        service.createRequestFeedback(requestId, type, appUser);

        verify(repository).existsByRequestIdAndType(requestId, type);
        verifyNoInteractions(queryService, mapper, dateService);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void createRequestFeedback_success() throws NoSuchFieldException, IllegalAccessException {
        String type = "type";
        String requestId = "requestId";
        String userId = "userId";
        LocalDateTime now = LocalDateTime.now();
        AppUser appUser = AppUser.builder()
            .firstName("firstName")
            .lastName("lastName")
            .userId(userId)
            .build();
        RequestFeedback requestFeedback = RequestFeedback.builder()
            .requestId(requestId)
            .status(RequestFeedbackStatus.NEW)
            .type(type)
            .createdOn(now)
            .createdBy("firstName lastName")
            .createdById(userId)
            .build();

        setRequestFeedbackEnabledValue(true);

        when(repository.existsByRequestIdAndType(requestId, type)).thenReturn(false);
        when(dateService.getLocalDateTime()).thenReturn(now);

        service.createRequestFeedback(requestId, type, appUser);

        verify(repository).existsByRequestIdAndType(requestId, type);
        verify(repository).save(requestFeedback);
        verify(dateService).getLocalDateTime();
        verifyNoInteractions(queryService, mapper);
        verifyNoMoreInteractions(dateService, repository);
    }

    @ParameterizedTest
    @MethodSource
    void getRequestFeedbackDetails(boolean expired, LocalDateTime createdOn, LocalDateTime now) {
        TestRequestFeedbackType type = TestRequestFeedbackType.TEST1;
        String requestId = "requestId";
        RequestFeedback requestFeedback = mock(RequestFeedback.class);
        RequestFeedbackDetailsDTO expected = mock(RequestFeedbackDetailsDTO.class);

        when(queryService.getRequestFeedback(requestId, type.getName())).thenReturn(requestFeedback);
        when(dateService.getLocalDateTime()).thenReturn(now);
        when(mapper.mapToDTO(requestFeedback, expired)).thenReturn(expected);
        when(requestFeedback.getCreatedOn()).thenReturn(createdOn);

        RequestFeedbackDetailsDTO requestFeedbackDetails = service.getRequestFeedbackDetails(requestId, type);

        assertThat(expected).isEqualTo(requestFeedbackDetails);

        verify(queryService).getRequestFeedback(requestId, type.getName());
        verify(dateService).getLocalDateTime();
        verify(mapper).mapToDTO(requestFeedback, expired);
        verify(requestFeedback).getCreatedOn();

        verifyNoInteractions(repository);
        verifyNoMoreInteractions(dateService, queryService, mapper, requestFeedback);
    }

    private static Stream<Arguments> getRequestFeedbackDetails() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
            Arguments.of(false, now, now),
            Arguments.of(false, now.plusMonths(1), now),
            Arguments.of(false, now.plusMonths(1).plusDays(1), now),
            Arguments.of(false, now.minusMonths(1), now),
            Arguments.of(true, now.minusMonths(1).minusDays(1), now)
        );
    }

    @Test
    void submitRequestFeedback() {
        LocalDateTime now = LocalDateTime.now();
        TestRequestFeedbackType type = TestRequestFeedbackType.TEST1;
        String requestId = "requestId";
        String userId = "userId";
        TestRequestFeedbackPayload payload = TestRequestFeedbackPayload.builder().build();
        AppUser appUser = AppUser.builder()
            .firstName("firstName")
            .lastName("lastName")
            .userId(userId)
            .build();

        RequestFeedbackStatus status = RequestFeedbackStatus.NEW;
        RequestFeedback requestFeedback = RequestFeedback.builder()
            .status(status)
            .createdOn(now)
            .build();

        RequestFeedback expectedRequestFeedback = RequestFeedback.builder()
            .createdOn(now)
            .submitterId(userId)
            .submitter("firstName lastName")
            .payload(payload)
            .submissionDate(now)
            .status(RequestFeedbackStatus.SUBMITTED)
            .build();

        when(queryService.getRequestFeedback(requestId, type.getName())).thenReturn(requestFeedback);
        when(dateService.getLocalDateTime()).thenReturn(now);

        service.submitRequestFeedback(requestId, type, payload, appUser);

        assertThat(expectedRequestFeedback).isEqualTo(requestFeedback);

        verify(queryService).getRequestFeedback(requestId, type.getName());
        verify(dateService).getLocalDateTime();
        verify(validator).validatePayloadType(type, payload);
        verify(validator).validateStatus(status);
        verify(validator).validateNotExpired(now);

        verifyNoInteractions(repository, mapper);
        verifyNoMoreInteractions(queryService, dateService);
    }

    private void setRequestFeedbackEnabledValue(boolean value) throws NoSuchFieldException, IllegalAccessException {
        Field field = RequestFeedbackService.class.getDeclaredField("requestFeedbackEnabled");
        field.setAccessible(true);
        field.set(service, value);
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @AllArgsConstructor
    @Data
    public static class TestRequestFeedbackPayload extends RequestFeedbackPayload {
    }

    @AllArgsConstructor
    public enum TestRequestFeedbackType implements RequestFeedbackType {
        TEST1,
        TEST2;

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public Class<? extends RequestFeedbackPayload> getPayloadClass() {
            return null;
        }
    }
}
