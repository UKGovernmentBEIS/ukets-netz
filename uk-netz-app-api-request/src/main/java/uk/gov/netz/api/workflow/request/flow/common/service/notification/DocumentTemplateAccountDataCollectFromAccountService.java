package uk.gov.netz.api.workflow.request.flow.common.service.notification;

public interface DocumentTemplateAccountDataCollectFromAccountService<T extends DocumentTemplateAccountData> {

	T collect(Long accountId);

}
