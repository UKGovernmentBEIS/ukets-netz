package uk.gov.netz.api.mireport.system.accountuserscontacts;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.mireport.system.EmptyMiReportSystemParams;
import uk.gov.netz.api.mireport.system.MiReportSystemGenerator;
import uk.gov.netz.api.mireport.system.MiReportSystemResult;
import uk.gov.netz.api.mireport.system.MiReportSystemType;
import uk.gov.netz.api.userinfoapi.UserInfoApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AccountUsersContactsReportGenerator<U extends AccountUserContact> implements MiReportSystemGenerator<EmptyMiReportSystemParams>  {
    private final UserInfoApi userInfoApi;

    public abstract List<U> findAccountUserContacts(EntityManager entityManager);

    public String getReportType() {
        return MiReportSystemType.LIST_OF_ACCOUNTS_USERS_CONTACTS;
    }

    @Transactional(readOnly = true)
    public MiReportSystemResult generateMiReport(EntityManager entityManager, EmptyMiReportSystemParams reportParams) {
        List<U> accountUserContacts = findAccountUserContacts(entityManager);
        Map<String, OperatorUserInfoDTO> operatorUsersInfo = getOperatorUserInfoByUserIds(accountUserContacts);

        List<U> results = accountUserContacts.stream()
            .map(accountUserContact -> {
                OperatorUserInfoDTO operatorUserInfo = operatorUsersInfo.get(accountUserContact.getUserId());
                appendUserDetails(accountUserContact, operatorUserInfo);
                return accountUserContact;
            }).collect(Collectors.toList());

        return AccountsUsersContactsMiReportResult.<U>builder()
            .reportType(getReportType())
            .columnNames(getColumnNames())
            .results(results)
            .build();
    }

    private void appendUserDetails(AccountUserContact accountUserContact, OperatorUserInfoDTO operatorUserInfo) {
        if (Optional.ofNullable(operatorUserInfo).isPresent()) {
            accountUserContact.setName(operatorUserInfo.getFullName());
            accountUserContact.setTelephone(operatorUserInfo.getTelephone());
            accountUserContact.setLastLogon(Optional.ofNullable(operatorUserInfo.getLastLoginDate())
                .map(AccountUsersContactsReportGenerator::formatLastLoginDate).orElse(null));
            accountUserContact.setEmail(operatorUserInfo.getEmail());
        }
    }

    private Map<String, OperatorUserInfoDTO> getOperatorUserInfoByUserIds(List<U> accountUserContacts) {
        List<String> userIds = accountUserContacts.stream().map(AccountUserContact::getUserId).collect(Collectors.toList());
        return userInfoApi.getUsersWithAttributes(userIds, OperatorUserInfoDTO.class)
            .stream()
            .collect(Collectors.toMap(OperatorUserInfoDTO::getId, Function.identity()));
    }

    private static String formatLastLoginDate(String lastLoginDate) {
        return LocalDateTime.parse(lastLoginDate, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss"));
    }

    public abstract List<String> getColumnNames();

}
