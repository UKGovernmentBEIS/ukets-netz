package uk.gov.netz.api.request.feedback.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.DateService;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackPayload;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackStatus;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackType;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestFeedbackValidatorTest {

    @InjectMocks
    private RequestFeedbackValidator validator;

    @Mock
    private DateService dateService;

    @ParameterizedTest
    @MethodSource
    void validateNotExpired(LocalDateTime createdOn, LocalDateTime now) {
        when(dateService.getLocalDateTime()).thenReturn(now);

        validator.validateNotExpired(createdOn);

        verify(dateService).getLocalDateTime();
        verifyNoMoreInteractions(dateService);
    }

    private static Stream<Arguments> validateNotExpired() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
            Arguments.of(now, now),
            Arguments.of(now.plusMonths(1), now),
            Arguments.of(now.plusMonths(1).plusDays(1), now),
            Arguments.of(now.minusMonths(1), now)
        );
    }

    @Test
    void validateNotExpired_throws_error() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdOn = now.minusMonths(1).minusDays(1);
        when(dateService.getLocalDateTime()).thenReturn(now);

        BusinessException ex = assertThrows(BusinessException.class, () ->
            validator.validateNotExpired(createdOn));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REQUEST_FEEDBACK_EXPIRED);

        verify(dateService).getLocalDateTime();
        verifyNoMoreInteractions(dateService);
    }

    @Test
    void validateStatus() {
        RequestFeedbackStatus status = RequestFeedbackStatus.NEW;

        validator.validateStatus(status);

        verifyNoInteractions(dateService);
    }

    @Test
    void validateStatus_throws_error() {
        RequestFeedbackStatus status = RequestFeedbackStatus.SUBMITTED;

        BusinessException ex = assertThrows(BusinessException.class, () ->
            validator.validateStatus(status));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REQUEST_FEEDBACK_ALREADY_SUBMITTED);

        verifyNoInteractions(dateService);
    }

    @Test
    void validatePayloadType() {
        RequestFeedbackType type = TestRequestFeedbackType.TEST1;
        RequestFeedbackPayload payload = Test1RequestFeedbackPayload.builder().build();

        validator.validatePayloadType(type, payload);

        verifyNoInteractions(dateService);
    }

    @Test
    void validatePayloadType_throws_error() {
        RequestFeedbackType type = TestRequestFeedbackType.TEST1;
        RequestFeedbackPayload payload = Test2RequestFeedbackPayload.builder().build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
            validator.validatePayloadType(type, payload));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REQUEST_FEEDBACK_INVALID_PAYLOAD);

        verifyNoInteractions(dateService);
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @AllArgsConstructor
    @Data
    public static class Test1RequestFeedbackPayload extends RequestFeedbackPayload {
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @AllArgsConstructor
    @Data
    public static class Test2RequestFeedbackPayload extends RequestFeedbackPayload {
    }

    @Getter
    @AllArgsConstructor
    public enum TestRequestFeedbackType implements RequestFeedbackType {
        TEST1(Test1RequestFeedbackPayload.class),
        TEST2(Test2RequestFeedbackPayload.class);

        private final Class<? extends RequestFeedbackPayload> payloadClass;

        @Override
        public String getName() {
            return this.name();
        }
    }
}