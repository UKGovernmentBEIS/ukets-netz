package uk.gov.netz.api.referencedata.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.referencedata.domain.County;
import uk.gov.netz.api.referencedata.repository.CountyRepository;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountyServiceTest {

    @InjectMocks
    private CountyService countyService;

    @Mock
    private CountyRepository countyRepository;

    @Test
    void getReferenceData() {
        List<County> TEST_COUNTIES = List.of(new County(1L, "AAAA"), new County(2L, "BBBB"));
        when(countyRepository.findAll()).thenReturn(TEST_COUNTIES);

        List<County> counties = countyService.getReferenceData();
        Assertions.assertEquals(TEST_COUNTIES.size(), counties.size());
        TEST_COUNTIES.forEach(test_country -> Assertions.assertTrue(counties.contains(test_country)));

        verify(countyRepository).findAll();
    }

    @Test
    void getAllCounties() {
        List<County> TEST_COUNTIES = List.of(new County(1L, "AAAA"), new County(2L, "BBBB"));
        when(countyRepository.findAll()).thenReturn(TEST_COUNTIES);

        List<County> counties = countyService.getReferenceData();
        Assertions.assertEquals(TEST_COUNTIES.size(), counties.size());
        TEST_COUNTIES.forEach(test_country -> Assertions.assertTrue(counties.contains(test_country)));

        verify(countyRepository).findAll();
    }
}
