package uk.gov.netz.api.workflow.request.core.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.util.List;
import java.util.Set;

import static uk.gov.netz.api.workflow.request.core.domain.RequestTask.NAMED_ENTITY_GRAPH_REQUEST_TASK_REQUEST;

@Repository
public interface RequestTaskRepository extends JpaRepository<RequestTask, Long> {

    @Transactional(readOnly = true)
    RequestTask findByProcessTaskId(String processTaskId);
    
    @Transactional(readOnly = true)
    RequestTask findByTypeCodeAndRequestId(String requestTaskType, String requestId);

    @Transactional(readOnly = true)
    List<RequestTask> findByRequestTypeAndAssignee(
            RequestType requestType, String assignee);
    
    @Transactional(readOnly = true)
    @EntityGraph(value = NAMED_ENTITY_GRAPH_REQUEST_TASK_REQUEST, type = EntityGraph.EntityGraphType.FETCH)
    List<RequestTask> findByAssignee(String assignee);

    @Transactional(readOnly = true)
    List<RequestTask> findByRequestId(String requestId);
    
    @Transactional(readOnly = true)
    @Query("select task "
            + "from RequestTask task "
            + "join Request req "
            + "on task.request.id = req.id "
            + "join RequestResource res "
            + "on task.request.id = res.request.id "
            + "where req.type = :requestType "
            + "and task.assignee = :assignee "
            + "and res.resourceType = 'ACCOUNT' "
            + "and res.resourceId = :accountId")
    List<RequestTask> findByRequestTypeAndAssigneeAndRequestAccountId(
        RequestType requestType, String assignee, Long accountId);

    @Transactional(readOnly = true)
    @Query("select task "
            + "from RequestTask task "
            + "join Request req "
            + "on task.request.id = req.id "
            + "join RequestResource res "
            + "on task.request.id = res.request.id "
            + "where task.assignee = :assignee "
            + "and res.resourceType = 'ACCOUNT' "
            + "and res.resourceId = :accountId")
	List<RequestTask> findByAssigneeAndRequestAccountId(String assignee, Long accountId);

    @Transactional(readOnly = true)
    @Query("select task "
        + "from RequestTask task "
        + "join Request req "
        + "on task.request.id = req.id "
        + "join RequestResource res "
        + "on task.request.id = res.request.id "
        + "where task.assignee IS NULL "
        + "and res.resourceType = 'ACCOUNT' "
        + "and res.resourceId = :accountId")
    List<RequestTask> findByUnassignedAndRequestAccountId(Long accountId);

    @Transactional(readOnly = true)
    @Query("select task "
            + "from RequestTask task "
            + "join RequestResource res "
            + "on task.request.id = res.request.id "
            + "where task.type in (:type) "
            + "and res.resourceType = 'ACCOUNT' "
            + "and res.resourceId = :accountId")
    List<RequestTask> findByTypeInAndRequestAccountId(Set<RequestTaskType> type, Long accountId);

    @Transactional(readOnly = true)
    @Query("select distinct res.resourceId "
    		+ "from RequestTask task "
            + "join RequestResource res "
            + "on task.request.id = res.request.id "
            + "join RequestResource res2 "
            + "on res.request.id = res2.request.id "
            + "where task.assignee = :userId "
            + "and task.type.code in (:taskTypes) "
            + "and res.resourceType = 'ACCOUNT' "
            + "and res2.resourceType = 'VERIFICATION_BODY' "
            + "and res2.resourceId = :vbId")
    List<Long> findAccountIdsByTaskAssigneeAndTaskTypeInAndVerificationBody(String userId,
                                                                            Set<String> taskTypes,
                                                                            Long vbId);

}
