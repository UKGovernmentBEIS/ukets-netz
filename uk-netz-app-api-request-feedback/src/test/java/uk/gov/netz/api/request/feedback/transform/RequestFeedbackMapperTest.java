package uk.gov.netz.api.request.feedback.transform;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackDetailsDTO;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RequestFeedbackMapperTest {

    private final RequestFeedbackMapper mapper = Mappers.getMapper(RequestFeedbackMapper.class);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void mapToDTO(boolean expired) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        RequestFeedbackStatus status = mock(RequestFeedbackStatus.class);

        RequestFeedback requestFeedback = RequestFeedback.builder()
            .id(1L)
            .requestId("requestId")
            .status(status)
            .type("type")
            .createdBy("createdBy")
            .createdById("createdById")
            .submitter("submitter")
            .submitterId("submitterId")
            .createdOn(now)
            .submissionDate(tomorrow)
            .build();

        RequestFeedbackDetailsDTO expected = RequestFeedbackDetailsDTO.builder()
            .id(1L)
            .requestId("requestId")
            .status(status)
            .type("type")
            .expired(expired)
            .build();

        RequestFeedbackDetailsDTO actual = mapper.mapToDTO(requestFeedback, expired);

        assertThat(actual).isEqualTo(expected);
    }

}