package uk.gov.netz.api.mireport.userdefined.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "mi_report_user_defined_category")
public class MiReportUserDefinedCategoryEntity {

    @Id
    @SequenceGenerator(name = "mi_report_user_defined_category_id_generator",
            sequenceName = "mi_report_user_defined_category_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mi_report_user_defined_category_id_generator")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "name", nullable = false, unique = true)
    @NotBlank
    private String name;

    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled=true;

}
