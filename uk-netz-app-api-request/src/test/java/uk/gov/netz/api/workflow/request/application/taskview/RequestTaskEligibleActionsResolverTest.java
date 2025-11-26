package uk.gov.netz.api.workflow.request.application.taskview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.flow.TestRequestTaskPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskEligibleActionsResolverTest {

    @InjectMocks
    private RequestTaskEligibleActionsResolver resolver;

    @Spy
    private ArrayList<RequestTaskActionEligibilityEvaluator> evaluators;

    @Mock
    private TestRequestTaskActionEligibilityEvaluator testEvaluator;

    @Test
    void resolveEligibleRequestTaskActions() {

        evaluators.add(testEvaluator);
        final TestRequestTaskPayload testRequestTaskPayload = TestRequestTaskPayload.builder().build();
        final RequestTask requestTask = RequestTask.builder()
                .type(RequestTaskType.builder()
                        .code("request task type")
                        .actionTypes(Set.of(RequestTaskActionType.builder().code("request task action type 1").build(),
                                RequestTaskActionType.builder().code("request task action type 2").build()
                        ))
                        .build())
                .payload(testRequestTaskPayload)
                .build();
        when(testEvaluator.getRequestTaskType()).thenReturn("request task type");
        when(testEvaluator.getRequestTaskActionTypes()).thenReturn(List.of("request task action type 2"));
        when(testEvaluator.isEligible(testRequestTaskPayload)).thenReturn(false);
        final List<String> actual = resolver.resolveEligibleRequestTaskActions(requestTask);
        assertThat(actual).containsExactly("request task action type 1");
    }


    private static class TestRequestTaskActionEligibilityEvaluator implements RequestTaskActionEligibilityEvaluator<TestRequestTaskPayload> {


        @Override
        public boolean isEligible(TestRequestTaskPayload requestTaskPayload) {
            return false;
        }

        @Override
        public String getRequestTaskType() {
            return null;
        }

        @Override
        public List<String> getRequestTaskActionTypes() {
            return null;
        }
    }
}
