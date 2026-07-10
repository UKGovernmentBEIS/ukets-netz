package uk.gov.netz.api.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFileTokenService {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    
    public String resolveGetFileUuid(String getFileToken) {
        return jwtTokenService.resolveTokenActionClaim(getFileToken, JwtTokenAction.GET_FILE);
    }

    public FileToken generateGetFileToken(String fileUuid) {
        long expirationMinutes = jwtProperties.getClaim().getGetFileAttachmentExpIntervalMinutes();
        String token = jwtTokenService.generateToken(JwtTokenAction.GET_FILE,
                fileUuid,
                expirationMinutes);
        return FileToken.builder().token(token).tokenExpirationMinutes(expirationMinutes).build();
    }
}
