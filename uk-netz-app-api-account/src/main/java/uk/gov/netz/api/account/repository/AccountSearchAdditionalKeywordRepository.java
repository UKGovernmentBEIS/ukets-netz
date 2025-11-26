package uk.gov.netz.api.account.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.account.domain.AccountSearchAdditionalKeyword;

public interface AccountSearchAdditionalKeywordRepository extends JpaRepository<AccountSearchAdditionalKeyword, Long> {

    @Transactional(readOnly = true)
    List<AccountSearchAdditionalKeyword> findByAccountId(Long accountId);

    void deleteAllByAccountId(Long accountId);
}