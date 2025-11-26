package uk.gov.netz.api.account.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountContactInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountContactVbInfoDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

/**
 * The Account Repository.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, AccountCustomRepository {

    @Transactional(readOnly = true)
    List<Account> findAllByIdIn(List<Long> ids);

    @Transactional(readOnly = true)
    //First issue: When the results exceed the page size Spring creates a count query to return the total number of the results. For the specific query below the count query produced is:
    //select count(acc) from Account acc left join acc.contacts contacts on contacts.{index} = :contactType where acc.verificationBodyId = :vbId
    //which is wrong and org.hibernate.query.sqm.ParsingException is thrown. By overriding the countQuery the application works as expected.
    //https://github.com/spring-projects/spring-data-jpa/issues/3133 has been opened for that.
    //Second issue: When a namedQuery is used the sql (and the countQuery) are produced at runtime and in a wrong way as mentioned above.
    //When the Query is inline, as done here, the queries (together with countQuery) are evaluated during deployment and produced correctly.
    @Query(value = "select new uk.gov.netz.api.account.domain.dto.AccountContactVbInfoDTO(acc.id, acc.name, acc.emissionTradingScheme, VALUE(contacts)) "
            + "from Account acc "
            + "left join acc.contacts contacts on KEY(contacts) = :contactType "
            + "where acc.verificationBodyId = :vbId "
            + "order by acc.name")
    Page<AccountContactVbInfoDTO> findAccountContactsByVbAndContactType(
            Pageable pageable, Long vbId, String contactType);

    @Transactional(readOnly = true)
    @Query(name = Account.NAMED_QUERY_FIND_ACCOUNT_CONTACTS_BY_ACCOUNT_IDS_AND_CONTACT_TYPE)
    List<AccountContactInfoDTO> findAccountContactsByAccountIdsAndContactType(
            List<Long> accountIds, String contactType);

    @Transactional(readOnly = true)
    @Query(name = Account.NAMED_QUERY_FIND_ACCOUNTS_BY_CONTACT_TYPE_AND_USER_ID)
    List<Account> findAccountsByContactTypeAndUserId(
            String contactType, String userId);

    @Transactional(readOnly = true)
    @Query(name = Account.NAMED_QUERY_FIND_IDS_BY_VB)
    List<Long> findAllIdsByVB(Long vbId);

    @Transactional(readOnly = true)
    @Query(name = Account.NAMED_QUERY_FIND_ACCOUNTS_WITH_CONTACTS_BY_VB_IN_LIST)
    Set<Account> findAllByVerificationWithContactsBodyIn(Set<Long> vbIds);

    @Transactional(readOnly = true)
    @Query("select acc.id "
            + "from Account acc "
            + "where acc.verificationBodyId = :vbId")
    List<Long> findAllIdsByVerificationBody(Long vbId);
    
    @Transactional(readOnly = true)
    @Query("select acc.competentAuthority "
            + "from Account acc "
            + "where acc.id in (:accountIds) ")
    Set<CompetentAuthorityEnum> findCAByIdIn(Set<Long> accountIds);
    
}
