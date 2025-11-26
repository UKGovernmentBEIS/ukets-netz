package uk.gov.netz.api.workflow.request.core.service;

import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;

import java.util.Set;

public interface InitializeRequestTaskHandler {

    RequestTaskPayload initializePayload(Request request);

    Set<String> getRequestTaskTypes();
}
