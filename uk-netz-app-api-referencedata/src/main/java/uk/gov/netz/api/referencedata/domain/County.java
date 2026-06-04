package uk.gov.netz.api.referencedata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * The country entity reference data.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Entity
@Table(name = "ref_county")
public class County implements ReferenceData {

    @Id
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank
    private String name;

    public County(@Positive Long id, @NotBlank String name) {
        this.id = Optional.of(id).filter(num -> num > 0).orElseThrow();
        this.name = Optional.of(name).filter(text -> Boolean.FALSE.equals(text.isBlank())).orElseThrow();
    }
}
