package uk.gov.netz.api.user.verifier.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.user.core.service.UserInfoService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@Service
@RequiredArgsConstructor
public class VerifierUserInfoService {

    private final UserInfoService userInfoService;

    public List<UserInfoDTO> getVerifierUsersInfo(List<String> userIds) {
        return userInfoService.getUsersInfo(userIds);
    }
}
