package uk.gov.netz.api.user.core.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.user.core.transform.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserInfoApi userInfoApi;
    private static final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    public List<UserInfoDTO> getUsersInfo(List<String> userIds) {
        return userInfoApi.getUsers(userIds).stream()
            .collect(Collectors.toMap(UserInfo::getId, user -> user))
            .values().stream()
            .map(userMapper::toUserInfoDTO).collect(Collectors.toList());
    }
}
