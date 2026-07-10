package uk.gov.netz.api.terms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @InjectMocks
    private TermsService termsService;

    @Mock
    private TermsRepository termsRepository;

    private static final String TERMS_URL = "myurl";

    @Test
    void getLatestTerms() {

        when(termsRepository.findLatestTerms()).thenReturn(buildMockTerms());

        Terms actual = termsService.getLatestTerms();

        Assertions.assertEquals(TERMS_URL, actual.getUrl());
    }

    private Terms buildMockTerms() {
        Terms terms = new Terms();
        terms.setId(1L);
        terms.setUrl(TERMS_URL);
        terms.setVersion((short) 1);
        return terms;
    }
}
