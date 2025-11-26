package uk.gov.netz.api.workflow.request.core.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.netz.api.workflow.request.core.domain.enumeration.SupportingTaskType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "request_task_type")
public class RequestTaskType {

	@Id
    @SequenceGenerator(name = "request_task_type_id_generator", sequenceName = "request_task_type_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_task_type_id_generator")
    private Long id;

	@EqualsAndHashCode.Include()
    @NotNull
    @Column(name = "code", unique = true)
    private String code;
	
    @Column(name = "assignable")
    private boolean assignable;
    
    @Column(name = "expiration_key")
    private String expirationKey;
    
    @Column(name = "supporting")
    @Enumerated(EnumType.STRING)
    private SupportingTaskType supporting;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    private RequestType requestType;
    
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "request_task_type_action_type",
        joinColumns = @JoinColumn(name = "request_task_type_id"),
        inverseJoinColumns = @JoinColumn(name = "request_task_action_type_id")
    )
    private Set<RequestTaskActionType> actionTypes = new HashSet<>();
    
    public boolean isExpirable() {
    	return expirationKey != null;
    }

    public boolean isPeerReview() {
        return supporting == SupportingTaskType.PEER_REVIEW;
    }

    public boolean isSupporting() {
        return supporting != null;
    }
}
