package uk.gov.netz.api.competentauthority;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetentAuthorityRepository<T extends CompetentAuthority>
		extends JpaRepository<CompetentAuthority, CompetentAuthorityEnum>, CompetentAuthorityCustomRepository<T> {

}
