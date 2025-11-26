package uk.gov.netz.api.account.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

public interface AccountSearchRepository extends JpaRepository<Account, Long> {

    @Transactional(readOnly = true)
    @Query(value = "select distinct acc "
            + "from Account acc "
            + "inner join AccountSearchAdditionalKeyword ask on ask.accountId = acc.id "
            + "where acc.id in (:accountIds) "
            + "and LOWER(ask.value) like CONCAT('%',:term,'%') ")
    Page<Account> searchAccounts(PageRequest pageRequest, List<Long> accountIds, String term);

    @Transactional(readOnly = true)
    @Query(value = "select distinct acc "
            + "from Account acc "
            + "inner join AccountSearchAdditionalKeyword ask on ask.accountId = acc.id "
            + "where acc.competentAuthority = :ca "
            + "and LOWER(ask.value) like CONCAT('%',:term,'%') ")
    Page<Account> searchAccounts(PageRequest pageRequest, CompetentAuthorityEnum ca, String term);

}
