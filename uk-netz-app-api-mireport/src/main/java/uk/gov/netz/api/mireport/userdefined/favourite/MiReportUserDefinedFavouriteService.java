package uk.gov.netz.api.mireport.userdefined.favourite;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedRepository;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Service
@AllArgsConstructor
public class MiReportUserDefinedFavouriteService {

    private final MiReportUserDefinedFavouriteRepository miReportUserDefinedFavouriteRepository;
    private final MiReportUserDefinedRepository miReportUserDefinedRepository;

    @Transactional
    public void addFavourite(AppUser appUser, Long miReportId) {
        if (!miReportUserDefinedRepository.existsById(miReportId)) {
            throw new BusinessException(RESOURCE_NOT_FOUND);
        }

        String userId = appUser.getUserId();
        if (miReportUserDefinedFavouriteRepository.existsByUserIdAndMiReportId(userId, miReportId)) {
            return;
        }

        miReportUserDefinedFavouriteRepository.save(
                MiReportUserDefinedFavouriteEntity.builder()
                        .userId(userId)
                        .miReportId(miReportId)
                        .build());
    }

    @Transactional
    public void removeFavourite(AppUser appUser, Long miReportId) {
        miReportUserDefinedFavouriteRepository.deleteByUserIdAndMiReportId(appUser.getUserId(), miReportId);
    }

    @Transactional(readOnly = true)
    public boolean isFavourite(AppUser appUser, Long miReportId) {
        return miReportUserDefinedFavouriteRepository.existsByUserIdAndMiReportId(appUser.getUserId(), miReportId);
    }
}
