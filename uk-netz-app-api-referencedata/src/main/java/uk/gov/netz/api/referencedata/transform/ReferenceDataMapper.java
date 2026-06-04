package uk.gov.netz.api.referencedata.transform;

import uk.gov.netz.api.referencedata.domain.ReferenceData;
import uk.gov.netz.api.referencedata.domain.dto.ReferenceDataDTO;

import java.util.List;

/**
 * Reference data mapper interface
 *
 * @param <E> the reference data entity
 * @param <D> the reference data DTO
 */
public interface ReferenceDataMapper<E extends ReferenceData, D extends ReferenceDataDTO> {

    D toDTO(E entity);

    List<D> toDTOs(List<E> entities);
}
