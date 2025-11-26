package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.service.AccountContactQueryService;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestAccountContactQueryService {

    private final AccountContactQueryService accountContactQueryService;
    private final UserInfoApi userInfoApi;
    
    public Optional<UserInfoDTO> getRequestAccountContact(Request request, String contactType) {
        return accountContactQueryService
            .findContactByAccountAndContactType(request.getAccountId(), contactType)
            .map(userInfoApi::getUserByUserId);
    }

    public Optional<UserInfoDTO> getRequestAccountPrimaryContact(Request request) {
        return getRequestAccountContact(request, AccountContactType.PRIMARY);
    }

    public Optional<UserInfoDTO> getRequestAccountServiceContact(Request request) {
        return getRequestAccountContact(request, AccountContactType.SERVICE);
    }

}
