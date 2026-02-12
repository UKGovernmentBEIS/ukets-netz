package uk.gov.netz.api.thirdpartydataprovider.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners({AuditingEntityListener.class})
@NamedQuery(
    name = ThirdPartyDataProvider.NAMED_QUERY_FIND_ALL_THIRD_PARTY_DATA_PROVIDERS,
    query = "select new uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO(tpdp.id, tpdp.name) "
        + "from ThirdPartyDataProvider tpdp")
public class ThirdPartyDataProvider {
    public static final String NAMED_QUERY_FIND_ALL_THIRD_PARTY_DATA_PROVIDERS = "ThirdPartyDataProvider.findAllThirdPartyDataProviders";

    @Id
    @SequenceGenerator(name = "third_party_data_provider_id_generator", sequenceName = "third_party_data_provider_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "third_party_data_provider_id_generator")
    private Long id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "client_id", unique = true)
    @NotBlank
    @EqualsAndHashCode.Include()
    private String clientId;

    @Column(name = "client_entity_id", unique = true)
    @NotBlank
    @EqualsAndHashCode.Include()
    private String clientEntityId;

    @NotNull
    @Column(name = "created_date")
    @CreatedDate
    private LocalDateTime createdDate;
    
}
