package uk.gov.netz.api.workflow.request.core.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;

@Service
@RequiredArgsConstructor
public class RequestTaskTypeService {

    private final RequestTaskTypeRepository requestTaskTypeRepository;

    public RequestTaskType findByCode(String code) {
        return requestTaskTypeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

}
