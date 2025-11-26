package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

public interface PreviewDocumentHandler {

    @Transactional(readOnly = true)
    FileDTO previewDocument(Long taskId, final DecisionNotification decisionNotification);

    List<String> getTypes();
}
