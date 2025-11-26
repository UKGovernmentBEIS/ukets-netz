package uk.gov.netz.api.authorization.rules.services.authorityinfo.providers;

import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

public interface DocumentTemplateAuthorityInfoProvider {
    CompetentAuthorityEnum getDocumentTemplateCaById(Long templateId);
}
