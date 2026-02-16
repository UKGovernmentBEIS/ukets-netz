package uk.gov.netz.api.mireport.system.outstandingrequesttasks;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OutstandingRequestTasksRepository {

    @Transactional(readOnly = true)
    <T extends OutstandingRequestTask> List<T> findOutstandingRequestTaskParams(EntityManager entityManager,
                                                                                OutstandingRegulatorRequestTasksMiReportParams params);

}
