package uk.gov.netz.api.mireport.system.executedactions;

import jakarta.persistence.EntityManager;

import java.util.List;

public interface ExecutedRequestActionsRepository {

    <T extends ExecutedRequestAction> List<T> findExecutedRequestActions(EntityManager entityManager,
                                                                         ExecutedRequestActionsMiReportParams reportParams);
}
