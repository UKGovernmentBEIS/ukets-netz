package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.service.AccountContactQueryService;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityRoleDTO;
import uk.gov.netz.api.authorization.operator.service.OperatorAuthorityQueryService;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestActionUserInfo;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.OfficialNoticeSendService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RequestActionUserInfoResolver {

    private final UserInfoApi userInfoApi;
    private final AccountContactQueryService accountContactQueryService;
    private final OperatorAuthorityQueryService operatorAuthorityQueryService;
    private final OfficialNoticeSendService officialNoticeSendService;

    @Transactional(readOnly = true)
    public String getUserFullName(final String userId) {
        return userInfoApi.getUserByUserId(userId).getFullName();
    }

    @Transactional(readOnly = true)
    public Map<String, RequestActionUserInfo> getUsersInfo(final Set<String> operators, final Request request) {

        final Set<UserInfoDTO> defaultOfficialNoticeRecipients = officialNoticeSendService.getOfficialNoticeToRecipients(request);

        final Set<String> otherUsers = defaultOfficialNoticeRecipients.stream()
            .map(UserInfoDTO::getUserId)
            .collect(Collectors.toSet());

        final Set<String> userIds = Stream.of(operators, otherUsers)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        return this.getUsersInfo(userIds, request.getAccountId());
    }

    @Transactional(readOnly = true)
    public Map<String, RequestActionUserInfo> getUsersInfo(final Set<String> operators, final String signatory, final Request request) {
        Set<UserInfoDTO> defaultOfficialNoticeRecipients = officialNoticeSendService.getOfficialNoticeToRecipients(request);

        Set<String> otherUsers = defaultOfficialNoticeRecipients.stream()
                .map(UserInfoDTO::getUserId)
                .collect(Collectors.toSet());
        otherUsers.add(signatory);

        final Set<String> userIds = Stream.of(operators, otherUsers)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return getUsersInfo(userIds, request.getAccountId());
    }

    @Transactional(readOnly = true)
    public RequestActionUserInfo getSignatoryUserInfo(final String signatory) {
        return RequestActionUserInfo.builder().name(getUserFullName(signatory)).build();
    }

    private Map<String, RequestActionUserInfo> getUsersInfo(final Set<String> userIds,
                                                            final Long accountId) {
        final Map<String, String> contactTypes =
                accountContactQueryService.findContactTypesByAccount(accountId);
        final List<AuthorityRoleDTO> authorities =
                operatorAuthorityQueryService.findOperatorUserAuthoritiesListByAccount(accountId);

        return userIds.stream().collect(
                Collectors.toMap(id -> id, id -> this.createUserInfo(id, contactTypes, authorities))
        );
    }

    private RequestActionUserInfo createUserInfo(final String userId,
                                                 final Map<String, String> contacts,
                                                 final List<AuthorityRoleDTO> authorities) {
        final Set<String> contactTypes = contacts.entrySet().stream()
                .filter(e -> e.getValue().equals(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        final String role = authorities
                .stream()
                .filter(a -> a.getUserId().equals(userId))
                .findFirst()
                .map(AuthorityRoleDTO::getRoleCode)
                .orElse(null);

        return RequestActionUserInfo.builder()
                .name(getUserFullName(userId))
                .roleCode(role)
                .contactTypes(contactTypes)
                .build();
    }
}
