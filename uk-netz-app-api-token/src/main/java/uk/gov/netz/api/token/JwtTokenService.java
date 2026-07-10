package uk.gov.netz.api.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public String generateToken(JwtTokenAction jwtTokenAction, String claimValue, long expirationInterval) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getClaim().getSecret());

        ZonedDateTime now = ZonedDateTime.now(clock);
        Date issued = Date.from(now.toInstant());
        Date expires = Date.from(now.plusMinutes(expirationInterval).toInstant());

        return JWT.create()
                .withIssuer(jwtProperties.getClaim().getIssuer())
                .withIssuedAt(issued)
                .withSubject(jwtTokenAction.getSubject())
                .withClaim(jwtTokenAction.getClaimName(), claimValue)
                .withAudience(jwtProperties.getClaim().getAudience())
                .withExpiresAt(expires)
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token, String subject) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getClaim().getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(jwtProperties.getClaim().getIssuer())
                .withSubject(subject)
                .build();

        try {
            return verifier.verify(token);
        } catch (TokenExpiredException e) {
            throw new BusinessException(ErrorCode.VERIFICATION_LINK_EXPIRED);
        } catch (JWTVerificationException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String resolveTokenActionClaim(String token, JwtTokenAction jwtTokenAction) {
        DecodedJWT decodedJwt = verifyToken(token, jwtTokenAction.getSubject());
        final String resolved = decodedJwt.getClaim(jwtTokenAction.getClaimName()).asString();
        if(!StringUtils.hasText(resolved)) {
	    	throw new BusinessException(ErrorCode.INVALID_TOKEN);
	    }
        return resolved;
    }
}
