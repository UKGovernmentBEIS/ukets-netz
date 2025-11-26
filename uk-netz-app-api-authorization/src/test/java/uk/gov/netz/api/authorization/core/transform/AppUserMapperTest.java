package uk.gov.netz.api.authorization.core.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_TASK_ASSIGNMENT;


@ExtendWith(MockitoExtension.class)
class AppUserMapperTest {
	
    private AppUserMapper cut = Mappers.getMapper(AppUserMapper.class);


    @Test
    void toappUser() {
        AuthorityDTO accountAuthority = buildAccountAuthority();
        AuthorityDTO caAuthority = buildCaAuthority();
        String roleType = RoleTypeConstants.OPERATOR;
        
        String userId = "userId";
        String email = "user@email.com";
        String firstName = "name";
        String lastName = "surname";

        AppUser expectedUser = getExpectedappUser(userId, email, firstName, lastName, accountAuthority, caAuthority, roleType);

        AppUser appUser = cut.toAppUser(userId, email, firstName, lastName, List.of(accountAuthority, caAuthority), roleType);

        assertThat(appUser).isEqualTo(expectedUser);
    }

    @Test
    void toappUser_no_authorities() {
    	String roleType = RoleTypeConstants.OPERATOR;
        
        String userId = "userId";
        String email = "user@email.com";
        String firstName = "name";
        String lastName = "surname";

        AppUser expectedUser = AppUser.builder()
            .userId(userId)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .roleType(roleType)
            .build();

        AppUser appUser = cut.toAppUser(userId, email, firstName, lastName, Collections.emptyList(), roleType);

        assertThat(appUser).isEqualTo(expectedUser);
    }

    private AppUser getExpectedappUser(String userId, String email, String firstName, String lastName, AuthorityDTO accountAuthority, AuthorityDTO caAuthority, String roleType) {
        return AppUser.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roleType(roleType)
                .authorities(List.of(
                        AppAuthority.builder()
                                .code(accountAuthority.getCode())
                                .accountId(accountAuthority.getAccountId())
                                .permissions(accountAuthority.getAuthorityPermissions())
                                .build(),
                        AppAuthority.builder()
                                .code(caAuthority.getCode())
                                .competentAuthority(caAuthority.getCompetentAuthority())
                                .permissions(caAuthority.getAuthorityPermissions())
                                .build()))
                .build();
    }

    private AuthorityDTO buildCaAuthority() {
        return AuthorityDTO.builder()
                .code("code2")
                .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
                .authorityPermissions(List.of(PERM_ACCOUNT_USERS_EDIT,
                        PERM_TASK_ASSIGNMENT))
                .build();
    }

    private AuthorityDTO buildAccountAuthority() {
        return AuthorityDTO.builder()
                .code("code1")
                .accountId(1L)
                .authorityPermissions(List.of(PERM_ACCOUNT_USERS_EDIT,
                        PERM_TASK_ASSIGNMENT))
                .build();
    }

//    private AccessToken buildAccessToken() {
//        Map<String, String> token = Map.of("sub", "userId", "email", "user@email.com", "given_name", "name", "family_name", "surname");
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.convertValue(token, AccessToken.class);
//    }
}