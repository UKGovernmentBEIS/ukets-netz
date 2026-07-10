package uk.gov.netz.api.competentauthority;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.LockMode;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CompetentAuthorityCustomRepositoryImpl implements CompetentAuthorityCustomRepository<CompetentAuthority> {

	@PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public Optional<CompetentAuthority> findByIdForUpdate(CompetentAuthorityEnum id) {
        return ((Query<CompetentAuthority>)entityManager.createQuery("select ca from CompetentAuthority ca where ca.id = :id"))
                .setLockMode("ac", LockMode.PESSIMISTIC_WRITE)
                .setTimeout(5000)
                .setParameter("id", id)
                .uniqueResultOptional();
    }
    
}
