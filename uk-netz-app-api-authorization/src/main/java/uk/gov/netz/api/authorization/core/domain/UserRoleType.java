package uk.gov.netz.api.authorization.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "au_user_role_type", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id" }))
public class UserRoleType {

	@Id
	@Column(name = "user_id")
	@EqualsAndHashCode.Include
	private String userId;

	@NotNull
	@Column(name = "role_type")
	private String roleType;

}
