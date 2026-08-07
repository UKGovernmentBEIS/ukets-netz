package uk.gov.netz.api.mireport.userdefined.favourite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "mi_report_user_defined_favourite",
        uniqueConstraints = @UniqueConstraint(name = "mi_report_user_defined_favourite_uc",
                columnNames = {"user_id", "mi_report_id"}))
public class MiReportUserDefinedFavouriteEntity {

    @Id
    @SequenceGenerator(name = "mi_report_user_defined_favourite_id_generator",
            sequenceName = "mi_report_user_defined_favourite_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mi_report_user_defined_favourite_id_generator")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false)
    @NotBlank
    private String userId;

    @EqualsAndHashCode.Include
    @Column(name = "mi_report_id", nullable = false)
    @NotNull
    private Long miReportId;
}
