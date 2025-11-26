package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.workflow.request.core.domain.Request;

public interface DocumentTemplateCommonParamsProvider {

    TemplateParams constructCommonTemplateParams(final Request request, final String signatory);
}
