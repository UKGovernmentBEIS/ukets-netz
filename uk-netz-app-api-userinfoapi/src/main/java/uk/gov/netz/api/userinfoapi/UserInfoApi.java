package uk.gov.netz.api.userinfoapi;

import java.util.List;
import java.util.Optional;

public interface UserInfoApi {
    UserInfoDTO getUserByUserId(String userId);
    Optional<UserInfoDTO> getUserByEmail(String email);
    List<UserInfo> getUsers(List<String> userIds);
    <T> List<T> getUsersWithAttributes(List<String> userIds, Class<T> attributesClazz);
}
