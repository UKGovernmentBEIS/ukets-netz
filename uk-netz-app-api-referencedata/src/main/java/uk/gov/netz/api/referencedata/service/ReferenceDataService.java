package uk.gov.netz.api.referencedata.service;

import uk.gov.netz.api.referencedata.domain.ReferenceData;

import java.util.List;

/**
 * The Reference Data Service.
 */
public interface ReferenceDataService<T extends ReferenceData> {
    /**
     * Get reference data
     * @return the list of the reference data
     */
    List<T> getReferenceData();
    
}
