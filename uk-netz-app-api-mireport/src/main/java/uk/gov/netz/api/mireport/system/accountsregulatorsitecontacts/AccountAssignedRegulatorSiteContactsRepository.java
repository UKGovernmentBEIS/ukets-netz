package uk.gov.netz.api.mireport.system.accountsregulatorsitecontacts;

import jakarta.persistence.EntityManager;

import java.util.List;

public interface AccountAssignedRegulatorSiteContactsRepository {

    <T extends AccountAssignedRegulatorSiteContact> List<T> findAccountAssignedRegulatorSiteContacts(EntityManager entityManager);
}
