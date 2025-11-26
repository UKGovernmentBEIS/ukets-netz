package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTaskTimeManagementService {

    private final RequestTaskRepository requestTaskRepository;

    public List<RequestTask> setDueDateToTasks(String requestId, String requestExpirationType, LocalDate dueDate) {
        List<RequestTask> requestTasks = findAssociatedTasks(requestId, requestExpirationType);
        requestTasks.forEach(requestTask -> requestTask.setDueDate(dueDate));
        return requestTasks;
    }
    
    public void pauseTasks(String requestId, String requestExpirationType) {
        List<RequestTask> requestTasks = findAssociatedTasks(requestId, requestExpirationType);
        requestTasks.forEach(requestTask -> requestTask.setPauseDate(LocalDate.now()));
    }
    
    public void unpauseTasksAndUpdateDueDate(String requestId, String requestExpirationType, LocalDate dueDate) {
        List<RequestTask> requestTasks = findAssociatedTasks(requestId, requestExpirationType);
        requestTasks.forEach(requestTask -> {
            requestTask.setPauseDate(null);
            requestTask.setDueDate(dueDate);
        });
    }
    
    private List<RequestTask> findAssociatedTasks(String requestId, String requestExpirationKey) {
        return requestTaskRepository.findByRequestId(requestId).stream()
                    .filter(task -> task.getType().isExpirable() &&
                                        task.getType().getExpirationKey().equals(requestExpirationKey))
                    .toList();
    }

}
