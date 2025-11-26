package uk.gov.netz.api.workflow.request.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RequestResourcePK.class)
@Entity
@Table(name = "request_resource")
public class RequestResource {

	@Id
    @NotNull
    @Column(name = "resource_type")
    private String resourceType;
	
    @NotNull
    @Column(name = "resource_id")
    private String resourceId;
	
    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id")
    private Request request;
}
