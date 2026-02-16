package uk.gov.netz.api.mireport.system.accountsregulatorsitecontacts;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.mireport.system.EmptyMiReportSystemParams;
import uk.gov.netz.api.mireport.system.MiReportSystemGenerator;
import uk.gov.netz.api.mireport.system.MiReportSystemResult;
import uk.gov.netz.api.mireport.system.MiReportSystemType;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AccountAssignedRegulatorSiteContactReportGenerator<U extends AccountAssignedRegulatorSiteContact> implements MiReportSystemGenerator<EmptyMiReportSystemParams> {
    private final UserInfoApi userInfoApi;

    public abstract List<U> findAccountAssignedRegulatorSiteContacts(EntityManager entityManager);

    public String getReportType() {
        return MiReportSystemType.LIST_OF_ACCOUNTS_ASSIGNED_REGULATOR_SITE_CONTACTS;
    }

    @Transactional(readOnly = true)
    public MiReportSystemResult generateMiReport(EntityManager entityManager, EmptyMiReportSystemParams reportParams) {
        List<U> accountAssignedRegulatorSiteContacts =
            findAccountAssignedRegulatorSiteContacts(entityManager);
        Map<String, UserInfo> userInfoMap = getUserInfoByUserIds(accountAssignedRegulatorSiteContacts);

        List<U> results = accountAssignedRegulatorSiteContacts.stream()
            .map(accountAssignedRegulatorSiteContact -> {
                if (Optional.ofNullable(accountAssignedRegulatorSiteContact.getUserId()).isPresent()) {
                    UserInfo userInfo = userInfoMap.get(accountAssignedRegulatorSiteContact.getUserId());
                    appendUserName(accountAssignedRegulatorSiteContact, userInfo);
                }
                return accountAssignedRegulatorSiteContact;
            }).collect(Collectors.toList());

        return AccountAssignedRegulatorSiteContactsMiReportResult.<U>builder()
            .reportType(getReportType())
            .columnNames(getColumnNames())
            .results(results)
            .build();
    }

    private Map<String, UserInfo> getUserInfoByUserIds(List<U> accountAssignedRegulatorSiteContacts) {
        List<String> userIds = accountAssignedRegulatorSiteContacts
            .stream()
            .map(AccountAssignedRegulatorSiteContact::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return userInfoApi.getUsers(userIds).stream()
            .collect(Collectors.toMap(UserInfo::getId, Function.identity()));
    }

    private void appendUserName(AccountAssignedRegulatorSiteContact accountAssignedRegulatorSiteContact, UserInfo userInfo) {
        accountAssignedRegulatorSiteContact.setAssignedRegulatorName(userInfo.getFullName());
    }

    public abstract List<String> getColumnNames();

}
