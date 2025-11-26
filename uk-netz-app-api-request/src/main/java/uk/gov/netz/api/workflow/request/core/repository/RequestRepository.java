package uk.gov.netz.api.workflow.request.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.taskview.RequestInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

    @Override
    Optional<Request> findById(String id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Request r where r.id = :id")
    Optional<Request> findByIdForUpdate(String id);
    
    @Transactional(readOnly = true)
    Request findByProcessInstanceId(String processInstanceId);

    @Transactional(readOnly = true)
    @Query("select req "
            + "from Request req "
            + "join RequestResource res "
            + "on req.id = res.request.id "
            + "where req.status = :status "
            + "and res.resourceType = 'ACCOUNT' "
            + "and res.resourceId = :accountId")
    List<Request> findByAccountIdAndStatus(Long accountId, String status);
    
    @Transactional(readOnly = true)
    @Query("select req "
            + "from Request req "
            + "join RequestResource res "
            + "on req.id = res.request.id "
            + "where req.status = :status "
            + "and res.resourceType = :resourceType "
            + "and res.resourceId = :resourceId")
    List<Request> findByResourceAndStatus(Long resourceId, String resourceType, String status);

    @Transactional(readOnly = true)
    @Query("select rq "
    		+ "from Request rq "
    		+ "join RequestResource res "
            + "on rq.id = res.request.id "
            + "where res.resourceType = 'ACCOUNT' "
    		+ "and res.resourceId = :accountId "
    		+ "and rq.status = :status "
    		+ "and rq.type.code = :requestTypeCode")
    List<Request> findByAccountIdAndTypeAndStatus(Long accountId, String requestTypeCode, String status);

    @Transactional(readOnly = true)
    List<Request> findByIdInAndStatus(Set<String> requestIds, String status);
    
    @Transactional(readOnly = true)
    @Query("select req "
            + "from Request req "
            + "join RequestResource res "
            + "on req.id = res.request.id "
            + "where res.resourceType = 'ACCOUNT' "
            + "and res.resourceId = :accountId")
    List<Request> findAllByAccountId(Long accountId);

    @Transactional(readOnly = true)
    @Query("select req "
            + "from Request req "
            + "join RequestResource res "
            + "on req.id = res.request.id "
            + "where res.resourceType = 'ACCOUNT' "
            + "and res.resourceId in (:accountIds)")
    List<Request> findAllByAccountIdIn(Set<Long> accountIds);
    
    @Transactional(readOnly = true)
    @Query("select "
    		+ "case when count(req)> 0 then true else false end "
    		+ "from Request req "
    		+ "join RequestResource res "
            + "on req.id = res.request.id "
            + "where req.status = :status "
            + "and req.type.code = :type "
            + "and res.resourceType = 'CA' "
            + "and res.resourceId = :#{#competentAuthority.name()}")
    boolean existsByTypeAndStatusAndCompetentAuthority(String type, String status, CompetentAuthorityEnum competentAuthority);

    @Transactional(readOnly = true)
    @Query("select "
    		+ "case when count(req)> 0 then true else false end "
    		+ "from Request req "
    		+ "join RequestResource res "
            + "on req.id = res.request.id "
            + "where req.type.code = :type "
            + "and res.resourceType = 'ACCOUNT' "
            + "and res.resourceId = :accountId")
    boolean existsByAccountIdAndType(Long accountId, String type);

    @Transactional(readOnly = true)
    @Query("select req "
            + "from Request req "
            + "join RequestResource res "
            + "on req.id = res.request.id "
            + "where req.type.code = :requestType "
            + "and res.resourceType = :resourceType "
            + "and res.resourceId = :resourceId")
    List<Request> findByRequestTypeAndResourceTypeAndResourceId(String requestType, String resourceType, String resourceId);

    @Transactional(readOnly = true)
    @Query("select new uk.gov.netz.api.workflow.request.application.taskview.RequestInfoDTO(req.id, req.type.code) " +
        "from Request req " +
        "join RequestResource res " +
        "on req.id = res.request.id " +
        "where res.resourceType = :resourceType " +
        "and res.resourceId = :resourceId " +
        "and req.type.code not in (:excludedRequestTypes) " +
        "order by req.creationDate")
    List<RequestInfoDTO> findByResourceTypeAndResourceIdAndTypeNotIn(List<String> excludedRequestTypes, String resourceType, String resourceId);
}
