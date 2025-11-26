package uk.gov.netz.api.workflow.request.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "request_type")
public class RequestType {

	@Id
    @SequenceGenerator(name = "request_type_id_generator", sequenceName = "request_type_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_type_id_generator")
    private Long id;

	@EqualsAndHashCode.Include()
    @NotNull
    @Column(name = "code", unique = true)
    private String code;
	
	@NotNull
    @Column(name = "description")
    private String description;
	
    @NotNull
    @Column(name = "process_definition_id")
    private String processDefinitionId;
    
    @NotNull
    @Column(name = "history_category")
    private String historyCategory;
    
    @Column(name = "hold_history")
    private boolean holdHistory;
    
    @Column(name = "displayed_in_progress")
    private boolean displayedInProgress;
    
    @Column(name = "cascadable")
    private boolean cascadable;
    
    @Column(name = "can_create_manually")
    private boolean canCreateManually;

    @NotNull
    @Column(name = "resource_type")
    private String resourceType;
}
