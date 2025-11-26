package uk.gov.netz.api.workflow.request.application.taskview;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;

import java.util.List;

public interface RequestTaskActionEligibilityEvaluator<T extends RequestTaskPayload> {

    boolean isEligible(T requestTaskPayload);

    String getRequestTaskType();

    List<String> getRequestTaskActionTypes();
}
