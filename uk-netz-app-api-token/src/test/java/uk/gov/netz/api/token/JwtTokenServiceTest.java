package uk.gov.netz.api.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {
    @InjectMocks
    private JwtTokenService jwtTokenService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JwtProperties jwtProperties;

    @Spy
    private final Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    @Test
    void generateToken() {
        String EMAIL = "email@email";
        JwtProperties.Claim claim = mock(JwtProperties.Claim.class);
        JwtTokenAction tokenAction = JwtTokenAction.USER_REGISTRATION;

        //mock
        when(jwtProperties.getClaim().getSecret()).thenReturn("secret");
        when(jwtProperties.getClaim().getAudience()).thenReturn("jwtAudience");
        when(jwtProperties.getClaim().getIssuer()).thenReturn("authServerUrl");

        //invoke
        jwtTokenService.generateToken(tokenAction, EMAIL, 5L);

        //verify mocks
        verify(jwtProperties.getClaim(), times(1)).getSecret();
        verify(jwtProperties.getClaim(), times(1)).getAudience();
        verify(jwtProperties.getClaim(), times(1)).getIssuer();

    }

    @Test
    void verifyToken() {
        final JwtTokenAction tokenAction = JwtTokenAction.USER_REGISTRATION;
        when(jwtProperties.getClaim().getSecret()).thenReturn("secret");
        when(jwtProperties.getClaim().getIssuer()).thenReturn("authServerUrl");

        // Mock token
        ZonedDateTime now = ZonedDateTime.now(fixedClock);
        String token = JWT.create()
                .withIssuer("authServerUrl")
                .withIssuedAt(Date.from(now.toInstant()))
                .withSubject(tokenAction.getSubject())
                .withAudience("jwtAudience")
                .withExpiresAt(
                        Date.from(ZonedDateTime.now(fixedClock).plusMinutes(1000L).toInstant()))
                .sign(Algorithm.HMAC256("secret"));

        DecodedJWT decodedJwt = jwtTokenService.verifyToken(token, tokenAction.getSubject());

        assertEquals(tokenAction.getSubject(), decodedJwt.getSubject());
    }

    @Test
    void verifyTokenExpired() {
        final String tokenSubject = "email@email";

        //mock
        when(jwtProperties.getClaim().getSecret()).thenReturn("secret");
        when(jwtProperties.getClaim().getIssuer()).thenReturn("authServerUrl");

        String token = JWT.create()
                .withIssuer("authServerUrl")
                .withIssuedAt(
                        Date.from(ZonedDateTime.now(fixedClock).minusDays(10).toInstant()))
                .withSubject(tokenSubject)
                .withAudience("jwtAudience")
                .withExpiresAt(
                        Date.from(ZonedDateTime.now(fixedClock).minusDays(5).toInstant()))
                .sign(Algorithm.HMAC256("secret"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            jwtTokenService.verifyToken(token, tokenSubject);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VERIFICATION_LINK_EXPIRED);
    }

    @Test
    void verifyTokenInvalid() {
        final JwtTokenAction tokenAction = JwtTokenAction.USER_REGISTRATION;

        //mock
        when(jwtProperties.getClaim().getSecret()).thenReturn("secret");
        when(jwtProperties.getClaim().getIssuer()).thenReturn("authServerUrl");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            jwtTokenService.verifyToken("xxxx", tokenAction.getSubject());
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void resolveTokenActionClaim() {
    }
}