package uk.gov.netz.api.user.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    @InjectMocks
    private UserInfoService userInfoService;

    @Mock
    private UserInfoApi userInfoApi;

    @Test
    void getUsersInfo() {
        String userId = "userId";
        String fn = "fn";
        String ln = "ln";

        UserInfo userInfo = UserInfo.builder().id(userId).firstName(fn).lastName(ln).enabled(true).build();
        List<UserInfoDTO> expectedUserInfoList = List.of(
            UserInfoDTO.builder().userId(userId).firstName(fn).lastName(ln).enabled(true).build()
        );

        when(userInfoApi.getUsers(List.of(userId))).thenReturn(List.of(userInfo));

        List<UserInfoDTO> actualUserInfoList = userInfoService
            .getUsersInfo(List.of(userId));

        assertThat(actualUserInfoList).containsExactlyElementsOf(expectedUserInfoList);

        verify(userInfoApi, times(1)).getUsers(List.of(userId));
    }
}