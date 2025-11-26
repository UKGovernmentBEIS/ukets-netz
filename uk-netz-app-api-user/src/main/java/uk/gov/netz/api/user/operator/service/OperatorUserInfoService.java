package uk.gov.netz.api.user.operator.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.user.core.service.UserInfoService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@Service
@RequiredArgsConstructor
public class OperatorUserInfoService {

    private final UserInfoService userInfoService;

    public List<UserInfoDTO> getOperatorUsersInfo(List<String> userIds) {
        return userInfoService.getUsersInfo(userIds);
    }
}
