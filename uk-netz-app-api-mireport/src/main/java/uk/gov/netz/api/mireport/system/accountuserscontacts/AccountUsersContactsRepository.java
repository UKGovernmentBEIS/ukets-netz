package uk.gov.netz.api.mireport.system.accountuserscontacts;

import jakarta.persistence.EntityManager;

import java.util.List;

public interface AccountUsersContactsRepository {

    <T extends AccountUserContact> List<T> findAccountUserContacts(EntityManager entityManager);
}
