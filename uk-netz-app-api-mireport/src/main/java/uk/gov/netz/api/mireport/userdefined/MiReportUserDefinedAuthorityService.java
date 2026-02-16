package uk.gov.netz.api.mireport.userdefined;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.MiReportUserDefinedAuthorityInfoProvider;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Service
@RequiredArgsConstructor
public class MiReportUserDefinedAuthorityService implements MiReportUserDefinedAuthorityInfoProvider {

	private final MiReportUserDefinedRepository miReportUserDefinedRepository;
	
	@Override
    @Transactional(readOnly = true)
	public CompetentAuthorityEnum getMiReportCaById(Long miReportUserDefinedId) {
		return miReportUserDefinedRepository.findById(miReportUserDefinedId)
                .map(MiReportUserDefinedEntity::getCompetentAuthority)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
	}

}
