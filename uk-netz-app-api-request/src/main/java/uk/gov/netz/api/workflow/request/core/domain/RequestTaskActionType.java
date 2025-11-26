package uk.gov.netz.api.workflow.request.core.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
@Table(name = "request_task_action_type")
public class RequestTaskActionType {

	@Id
    @SequenceGenerator(name = "request_task_action_type_id_generator", sequenceName = "request_task_action_type_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_task_action_type_id_generator")
    private Long id;

	@EqualsAndHashCode.Include()
    @NotNull
    @Column(name = "code", unique = true)
    private String code;
	
	@Column(name = "blocked_by_payment")
    private boolean blockedByPayment;
	
	@Builder.Default
    @ManyToMany(mappedBy = "actionTypes")
    private Set<RequestTaskType> requestTaskTypes = new HashSet<>();
	
}
