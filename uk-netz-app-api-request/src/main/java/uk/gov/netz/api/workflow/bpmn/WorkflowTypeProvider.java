package uk.gov.netz.api.workflow.bpmn;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.workflow.request.core.domain.Request;

@Repository
public interface WorkflowTypeProvider extends JpaRepository<Request, String> {

    @Transactional(readOnly = true)
    @Query("select task.request.engine "
    		+ "from RequestTask task "
            + "where task.processTaskId = :processTaskId")
    WorkflowEngineType findWorkflowEngineByProcessTaskId(String processTaskId);

    @Transactional(readOnly = true)
    @Query("select req.engine "
            + "from Request req "
            + "where req.id = :requestId")
    WorkflowEngineType findWorkflowEngineByRequestId(String requestId);

    @Transactional(readOnly = true)
    @Query("select req.engine "
            + "from Request req "
            + "where req.processInstanceId = :processInstanceId")
    WorkflowEngineType findWorkflowEngineByProcessInstanceId(String processInstanceId);
}
