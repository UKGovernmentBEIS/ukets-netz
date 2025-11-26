package uk.gov.netz.api.account.service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.transform.AccountSearchResultMapper;

@Validated
public abstract class AccountBaseSearchService<T extends Account> {

	private final AccountSearchResultMapper accountSearchResultMapper = Mappers.getMapper(AccountSearchResultMapper.class);
	
    protected String getSearchTerm(AccountSearchCriteria accountSearchCriteria) {
        return accountSearchCriteria.getTerm() != null ? accountSearchCriteria.getTerm().toLowerCase().trim() : "";
    }

    protected PageRequest getPageRequest(@Valid AccountSearchCriteria searchCriteria) {
        return PageRequest.of(
        		searchCriteria.getPaging().getPageNumber(),
        		searchCriteria.getPaging().getPageSize(),
        		Optional.ofNullable(searchCriteria.getSortBy())
		        	    .map(sortBy -> Sort.by(
		        	        Objects.requireNonNullElse(searchCriteria.getDirection(), Direction.ASC),
		        	        sortBy.getEntityProperty()))
		        	    .orElse(Sort.unsorted())
                );
    }
    
    protected AccountSearchResults buildAccountSearchResults(Page<T> pageResults) {
    	if(pageResults.isEmpty()) {
    		return AccountSearchResults.emptyAccountSearchResults();
    	} else {
    		return AccountSearchResults.builder()
                    .accounts(pageResults.toList().stream()
                    		.map(accountSearchResultMapper::toAccountInfoDTO)
                            .collect(Collectors.toList()))
                    .total(pageResults.getTotalElements())
                    .build();
    	}
    }
}