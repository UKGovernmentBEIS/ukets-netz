package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.service.CaExternalContactService;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DecisionNotificationUsersService {

    private final UserInfoApi userInfoApi;
    private final CaExternalContactService caExternalContactService;

    public List<String> findUserEmails(DecisionNotification decisionNotification) {
        final List<String> operatorEmails = userInfoApi
            .getUsers(new ArrayList<>(decisionNotification.getOperators())).stream()
            .map(UserInfo::getEmail)
            .toList();
        
        final List<String> externalContactEmails = caExternalContactService
                .getCaExternalContactEmailsByIds(decisionNotification.getExternalContacts());

        return Stream
                .concat(operatorEmails.stream(), externalContactEmails.stream())
                .collect(Collectors.toList());
    }
}
