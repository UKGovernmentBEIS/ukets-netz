package uk.gov.netz.api.competentauthority;

import java.util.Optional;

public interface CompetentAuthorityCustomRepository<T extends CompetentAuthority> {

	Optional<T> findByIdForUpdate(CompetentAuthorityEnum id);
	
}
