package uk.gov.netz.api.workflow.request.core.transform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestActionCustomMapperHandler {

    private final List<RequestActionCustomMapper> mappers;

    public Optional<RequestActionCustomMapper> getMapper(final String actionType, final String roleType) {
        
        return mappers.stream().filter(m -> m.getRequestActionType().equals(actionType) &&
                                            m.getUserRoleTypes().contains(roleType))
                      .findFirst();
    }
}
