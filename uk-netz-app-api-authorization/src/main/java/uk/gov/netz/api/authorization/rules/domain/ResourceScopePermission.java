package uk.gov.netz.api.authorization.rules.domain;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "au_resource_scope_permission")
public class ResourceScopePermission {

    @EqualsAndHashCode.Exclude
    @Id
    @SequenceGenerator(name = "au_resource_scope_permission_id_generator", sequenceName = "au_resource_scope_permission_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "au_resource_scope_permission_id_generator")
    private Long id;
    
    @NotNull
    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_sub_type")
    private String resourceSubType;
    
    @NotNull
    @Column(name = "permission")
    private String permission;

    @NotNull
    @Column(name = "scope")
    private String scope;
    
    @Column(name = "role_type")
    private String roleType;
    
}
