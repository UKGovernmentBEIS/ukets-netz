package uk.gov.netz.api.workflow.request.core.transform;

import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestActionDTO;

import java.util.Set;

public interface RequestActionCustomMapper {

    RequestActionDTO toRequestActionDTO(RequestAction requestAction);

    String getRequestActionType();

    Set<String> getUserRoleTypes();
}
