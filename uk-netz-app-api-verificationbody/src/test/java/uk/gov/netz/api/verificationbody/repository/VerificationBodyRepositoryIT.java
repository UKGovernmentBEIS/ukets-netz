package uk.gov.netz.api.verificationbody.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.AuditConfiguration;
import uk.gov.netz.api.common.domain.EmissionTradingScheme;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.verificationbody.domain.Address;
import uk.gov.netz.api.verificationbody.domain.VerificationBody;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyNameInfoDTO;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, AuditConfiguration.class})
class VerificationBodyRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private VerificationBodyRepository repo;

    @Autowired
    EntityManager entityManager;

    @Test
    void findActiveVerificationBodiesAccreditedToType() {
        createVerificationBody("vb1", "accredRefNum1",
            VerificationBodyStatus.ACTIVE, Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME));

        createVerificationBody("vb2", "accredRefNum2",
            VerificationBodyStatus.ACTIVE, Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME_2));

        createVerificationBody("vb3", "accredRefNum3",
            VerificationBodyStatus.DISABLED, Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME_2));

        flushAndClear();

        EmissionTradingScheme vbType = TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME;

        //invoke
        List<VerificationBodyNameInfoDTO> result = repo.findActiveVerificationBodiesAccreditedToEmissionTradingScheme(vbType.getName());

        //assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("vb1");
    }

    @Test
    void findByIdEagerEmissionTradingSchemes() {
        Set<EmissionTradingScheme> emissionTradingSchemes = Set.of(
        		TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME,
        		TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME_2
        );
        VerificationBody vb = createVerificationBody(
            "vb1",
            "accredRefNum1",
            VerificationBodyStatus.ACTIVE,
            emissionTradingSchemes
        );

        flushAndClear();

        //invoke
        Optional<VerificationBody> result = repo.findByIdEagerEmissionTradingSchemes(vb.getId());

        //assert
        assertThat(result)
                .isNotEmpty()
                .contains(vb);
        assertThat(result.get().getEmissionTradingSchemes()).
            containsExactlyInAnyOrder(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME.getName(), TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME_2.getName());
    }

    @Test
    void existsByIdAndStatus_true() {
        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1", VerificationBodyStatus.ACTIVE, Set.of());
        entityManager.persist(vb);
        flushAndClear();

        assertThat(repo.existsByIdAndStatus(vb.getId(), VerificationBodyStatus.ACTIVE)).isTrue();
    }

    @Test
    void existsByIdAndStatus_false() {
        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1", VerificationBodyStatus.DISABLED, Set.of());
        entityManager.persist(vb);
        flushAndClear();

        assertThat(repo.existsByIdAndStatus(vb.getId(), VerificationBodyStatus.ACTIVE)).isFalse();
    }

    @Test
    void existsByIdAndStatusNot_false() {
        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1", VerificationBodyStatus.DISABLED, Set.of());
        entityManager.persist(vb);
        flushAndClear();

        assertThat(repo.existsByIdAndStatusNot(vb.getId(), VerificationBodyStatus.DISABLED)).isFalse();
    }

    @Test
    void existsByIdAndStatusNot_true() {
        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1", VerificationBodyStatus.ACTIVE, Set.of());
        entityManager.persist(vb);
        flushAndClear();

        assertThat(repo.existsByIdAndStatusNot(vb.getId(), VerificationBodyStatus.DISABLED)).isTrue();
    }

    @Test
    void isVerificationBodyAccreditedToEmissionTradingScheme_return_true() {
        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1",
            VerificationBodyStatus.ACTIVE, Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME));

        flushAndClear();

        assertThat(repo.isVerificationBodyAccreditedToEmissionTradingScheme(vb.getId(), TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME.getName())).isTrue();
    }

//    @Test
//    void isVerificationBodyAccreditedToEmissionTradingScheme_return_false() {
//        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1",
//            VerificationBodyStatus.ACTIVE, Set.of(mock(EmissionTradingScheme.class)));
//
//        flushAndClear();
//
//        assertThat(repo.isVerificationBodyAccreditedToEmissionTradingScheme(vb.getId(), mock(EmissionTradingScheme.class))).isFalse();
//    }
//
//    @Test
//    void isVerificationBodyAccreditedToEmissionTradingScheme_not_active_return_false() {
//        VerificationBody vb = createVerificationBody("vb1", "accredRefNum1",
//            VerificationBodyStatus.DISABLED, Set.of(mock(EmissionTradingScheme.class)));
//
//        flushAndClear();
//
//        assertThat(repo.isVerificationBodyAccreditedToEmissionTradingScheme(vb.getId(), mock(EmissionTradingScheme.class))).isFalse();
//    }

    @Test
    void findByIdNot(){
        VerificationBody vb1 = createVerificationBody("vb1", "accredRefNum1",
            VerificationBodyStatus.ACTIVE, Set.of(mock(EmissionTradingScheme.class)));

        VerificationBody vb2 = createVerificationBody("vb2", "accredRefNum2",
            VerificationBodyStatus.ACTIVE, Set.of(mock(EmissionTradingScheme.class)));

        flushAndClear();

        //invoke
        List<VerificationBody> result = repo.findByIdNot(vb1.getId());

        //assert
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(vb2);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private VerificationBody createVerificationBody(String name, String accreditationRefNum, VerificationBodyStatus status,
                                                    Set<EmissionTradingScheme> emissionTradingSchemes) {
        VerificationBody vb =
                VerificationBody.builder()
                    .name(name)
                    .status(status)
                    .address(Address.builder().city("city").country("GR").line1("line1").postcode("postcode").build())
                    .accreditationReferenceNumber(accreditationRefNum)
                    .emissionTradingSchemes(emissionTradingSchemes.stream().map(EmissionTradingScheme::getName).collect(Collectors.toSet()))
                    .build();
        entityManager.persist(vb);
        return vb;
    }
}
