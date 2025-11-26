package uk.gov.netz.api.workflow.request.flow.common.service;

import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

public abstract class AccountIdBasedRequestIdGenerator implements RequestIdGenerator {

    @Override
    public String generate(RequestParams params) {
        return String.format("%s%05d", getPrefix(), params.getAccountId());
    }
}
