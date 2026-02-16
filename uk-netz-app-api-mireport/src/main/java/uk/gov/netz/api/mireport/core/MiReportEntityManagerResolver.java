package uk.gov.netz.api.mireport.core;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Service
public class MiReportEntityManagerResolver implements InitializingBean {

	/** Autowired can be used instead of @PersistenceContext based on spring documentation:
    <a href="https://docs.spring.io/spring-data/jpa/reference/repositories/custom-implementations.html">...</a>
    When working with multiple EntityManager instances and custom repository implementations,
    you need to wire the correct EntityManager into the repository implementation class.
    You can do so by explicitly naming the EntityManager in the @PersistenceContext annotation or,
    if the EntityManager is @Autowired, by using @Qualifier.
    */
   private final EntityManager reportEaEntityManager;
   private final EntityManager reportSepaEntityManager;
   private final EntityManager reportNieaEntityManager;
   private final EntityManager reportOpredEntityManager;
   private final EntityManager reportNrwEntityManager;
   
   private final Map<CompetentAuthorityEnum, EntityManager> caToEntityManagerMap = new EnumMap<>(CompetentAuthorityEnum.class);

   public MiReportEntityManagerResolver(@Nullable @Qualifier("reportEaEntityManager") EntityManager reportEaEntityManager,
                                   @Nullable @Qualifier("reportSepaEntityManager") EntityManager reportSepaEntityManager,
                                   @Nullable @Qualifier("reportNieaEntityManager") EntityManager reportNieaEntityManager,
                                   @Nullable @Qualifier("reportOpredEntityManager") EntityManager reportOpredEntityManager,
                                   @Nullable @Qualifier("reportNrwEntityManager") EntityManager reportNrwEntityManager) {
       this.reportEaEntityManager = reportEaEntityManager;
       this.reportSepaEntityManager = reportSepaEntityManager;
       this.reportNieaEntityManager = reportNieaEntityManager;
       this.reportOpredEntityManager = reportOpredEntityManager;
       this.reportNrwEntityManager = reportNrwEntityManager;
   }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		caToEntityManagerMap.put(CompetentAuthorityEnum.ENGLAND, reportEaEntityManager);
        caToEntityManagerMap.put(CompetentAuthorityEnum.SCOTLAND, reportSepaEntityManager);
        caToEntityManagerMap.put(CompetentAuthorityEnum.NORTHERN_IRELAND, reportNieaEntityManager);
        caToEntityManagerMap.put(CompetentAuthorityEnum.OPRED, reportOpredEntityManager);
        caToEntityManagerMap.put(CompetentAuthorityEnum.WALES, reportNrwEntityManager);
	}
	
	public EntityManager resolveByCA(CompetentAuthorityEnum ca) {
		return caToEntityManagerMap.get(ca);
	}

}
