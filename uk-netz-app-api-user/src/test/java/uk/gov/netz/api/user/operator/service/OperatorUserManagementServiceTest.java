package uk.gov.netz.api.user.operator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.operator.service.OperatorAuthorityQueryService;
import uk.gov.netz.api.common.domain.PhoneNumberDTO;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.user.core.service.UserSecuritySetupService;
import uk.gov.netz.api.user.operator.domain.OperatorUserDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserStatusDTO;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorUserManagementServiceTest {

	@InjectMocks
        private OperatorUserManagementService service;
	
	@Mock
	private OperatorUserAuthService operatorUserAuthService;
	
	@Mock
	private OperatorAuthorityQueryService operatorAuthorityQueryService;
	
	@Mock
	private UserSecuritySetupService userSecuritySetupService;
	
	@Test
	void updateOperatorUser() {
		OperatorUserDTO operatorUserDTO = buildOperatorUserDTO();
		AppUser appUser = AppUser.builder().email("email").build();

		service.updateOperatorUser(appUser,operatorUserDTO);

		verify(operatorUserAuthService, times(1)).updateUser(operatorUserDTO);
	}

	@Test
	void updateOperatorUser_not_current_user() {
		OperatorUserDTO operatorUserDTO = buildOperatorUserDTO();
		AppUser appUser = AppUser.builder().email("email1").build();

		BusinessException businessException = assertThrows(BusinessException.class,
				() -> service.updateOperatorUser(appUser,operatorUserDTO));

		assertEquals(ErrorCode.USER_NOT_LOGGED_IN_USER, businessException.getErrorCode());
		verifyNoInteractions(operatorUserAuthService);
	}

	@Test
	void getOperatorUserByAccountAndIdTestDifferentUserWithPermission() {
		final Long accountId = 1L;
		final String userId = "userId";
		OperatorUserDTO operatorUserDTO = OperatorUserDTO.builder()
				.firstName("fname")
				.lastName("lname")
				.email("email")
				.phoneNumber(PhoneNumberDTO.builder().number("123456").build())
				.build();
		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.ACTIVE)
				.build();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authorityDTO));
		when(operatorUserAuthService.getUserById(userId)).thenReturn(operatorUserDTO);

		// Invoke
		final OperatorUserStatusDTO actual = service.getOperatorUserByAccountAndId(accountId, userId);

		assertThat(actual.getAuthorityStatus()).isEqualTo(AuthorityStatus.ACTIVE);
		// Verify
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId,accountId);
		verify(operatorUserAuthService, times(1)).getUserById(userId);
	}

	@Test
	void getOperatorUserByAccountAndIdTestUserNotExists() {
		final Long accountId = 1L;
		final String userId = "userId";

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.empty());

		// Invoke
		BusinessException businessException = assertThrows(BusinessException.class,
				() -> service.getOperatorUserByAccountAndId(accountId, userId));

		// Verify
		assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT, businessException.getErrorCode());
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verifyNoInteractions(operatorUserAuthService);
	}

	@Test
	void getOperatorUserByAccountAndIdTestStatusNotActive() {
		final Long accountId = 1L;
		final String userId = "userId";
		OperatorUserDTO operatorUserDTO = OperatorUserDTO.builder()
				.firstName("fname")
				.lastName("lname")
				.email("email")
				.phoneNumber(PhoneNumberDTO.builder().number("123456").build())
				.build();
		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.PENDING)
				.build();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authorityDTO));
		when(operatorUserAuthService.getUserById(userId)).thenReturn(operatorUserDTO);
		// Invoke
		final OperatorUserStatusDTO actual = service.getOperatorUserByAccountAndId(accountId, userId);

		// Verify
		assertThat(actual.getAuthorityStatus()).isEqualTo(AuthorityStatus.PENDING);
		// Verify
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId,accountId);
		verify(operatorUserAuthService, times(1)).getUserById(userId);
	}

	@Test
	void updateOperatorUserByAccountAndIdTestDifferentUserWithPermission() {
		final Long accountId = 1L;
		final String userId = "userId";
		OperatorUserDTO operatorUserDTO = buildOperatorUserDTO();

		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.ACTIVE)
				.build();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authorityDTO));

		// Invoke
		service.updateOperatorUserByAccountAndId(accountId, userId, operatorUserDTO);

		// Verify
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verify(operatorUserAuthService, times(1)).updateUser(operatorUserDTO);
	}

	@Test
	void updateOperatorUserByAccountAndIdTestUserNotExists() {
		final Long accountId = 1L;
		final String userId = "userId";
		OperatorUserDTO operatorUserDTO = buildOperatorUserDTO();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.empty());

		// Invoke
		BusinessException businessException = assertThrows(BusinessException.class,
				() -> service.updateOperatorUserByAccountAndId(accountId, userId, operatorUserDTO));

		// Verify
		assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT, businessException.getErrorCode());
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verify(operatorUserAuthService, never()).updateUser(Mockito.any(OperatorUserDTO.class));
	}

	@Test
	void updateOperatorUserByAccountAndIdTestAccountNotExists() {
		final Long accountId = 1L;
		final String userId = "userId";
		OperatorUserDTO operatorUserDTO = buildOperatorUserDTO();

		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.PENDING)
				.build();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authorityDTO));

		// Invoke
		BusinessException businessException = assertThrows(BusinessException.class,
				() -> service.updateOperatorUserByAccountAndId(accountId, userId, operatorUserDTO));

		// Verify
		assertEquals(ErrorCode.USER_INVALID_STATUS, businessException.getErrorCode());
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verify(operatorUserAuthService, never()).updateUser(Mockito.any(OperatorUserDTO.class));
	}
	
	@Test
	void resetOperator2Fa() {
		final Long accountId = 1L;
		final String userId = "userId";

		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.ACTIVE)
				.build();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authorityDTO));

		// Invoke
		service.resetOperator2Fa(accountId, userId);

		// Verify
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verify(userSecuritySetupService, times(1)).resetUser2Fa(userId);
	}
	
	@Test
	void resetOperator2Fa_user_not_related_to_account() {
		final Long accountId = 1L;
		final String userId = "userId";

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.empty());

		BusinessException businessException = assertThrows(BusinessException.class,
				() -> service.resetOperator2Fa(accountId, userId));

		assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT, businessException.getErrorCode());
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verify(userSecuritySetupService, never()).resetUser2Fa(anyString());
	}

	@Test
	void resetOperator2Fa_user_status_not_active() {
		final Long accountId = 1L;
		final String userId = "userId";

		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.PENDING)
				.build();

		when(operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authorityDTO));

		BusinessException businessException = assertThrows(BusinessException.class,
				() -> service.resetOperator2Fa(accountId, userId));

		assertEquals(ErrorCode.USER_INVALID_STATUS, businessException.getErrorCode());
		verify(operatorAuthorityQueryService, times(1)).findAuthorityByUserIdAndAccountId(userId, accountId);
		verify(userSecuritySetupService, never()).resetUser2Fa(anyString());
	}
	
	private OperatorUserDTO buildOperatorUserDTO() {
		return OperatorUserDTO.builder()
				.firstName("fn")
				.lastName("ln")
				.email("email")
				.build();
	}
}
