package uk.gov.netz.api.configuration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@EqualsAndHashCode
@Table(name = "configuration")
public class ConfigurationEntity {

	@Id
	@Column(name = "key")
	@NotBlank
	private String key;

	@Column(name = "value")
	private String value;
	
	@Column(name = "type")
	@NotNull
	@Enumerated(value=EnumType.STRING)
	private ConfigurationValueType type;
	
}
