package uk.gov.netz.api.workflow.request.flow.common.service;

import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

import java.util.List;

/**
 * Generates request id according to the request type.
 */
public interface RequestIdGenerator {

    String generate(RequestParams params);

    List<String> getTypes();

    String getPrefix();
}
