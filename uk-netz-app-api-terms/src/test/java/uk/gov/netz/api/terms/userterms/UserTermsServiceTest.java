package uk.gov.netz.api.terms.userterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTermsServiceTest {
    @InjectMocks
    private UserTermsService service;

    @Mock
    private UserTermsVersionRepository userTermsVersionRepository;

    @Test
    void updateUserTerms_entry_exists() {
        String userId = "userId";
        UserTermsVersion userTermsVersion = UserTermsVersion.builder()
                .id(userId)
                .version((short)1)
                .build();
        when(userTermsVersionRepository.findById(userId)).thenReturn(Optional.of(userTermsVersion));

        service.updateUserTerms(userId, (short)2);

        assertEquals((short)2, userTermsVersion.getVersion());
    }

    @Test
    void updateUserTerms_no_entry_exists() {
        String userId = "userId";
        UserTermsVersion userTermsVersion = UserTermsVersion.builder()
                .id(userId)
                .version((short)1)
                .build();
        when(userTermsVersionRepository.findById(userId)).thenReturn(Optional.empty());

        service.updateUserTerms(userId, (short)2);

        verify(userTermsVersionRepository, times(1)).save(userTermsVersion);
    }

    @Test
    void getUserTerms() {
        String userId = "userId";
        UserTermsVersion userTermsVersion = UserTermsVersion.builder()
                .id(userId)
                .version((short)1)
                .build();
        when(userTermsVersionRepository.findById(userId)).thenReturn(Optional.of(userTermsVersion));

        Optional<Short> actual = service.getUserTerms(userId);

        Assertions.assertTrue(actual.isPresent());
        assertEquals((short)1, actual.get());
    }
    
    @Test
    void deleteUserTerms() {
    	String userId = "userId";
    	service.deleteUserTerms(userId);
    	verify(userTermsVersionRepository, times(1)).deleteById(userId);
    }
}