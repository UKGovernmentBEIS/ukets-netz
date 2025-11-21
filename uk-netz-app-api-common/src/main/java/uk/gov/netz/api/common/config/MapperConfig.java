package uk.gov.netz.api.common.config;

import org.mapstruct.Builder;

/**
 * Configuration source for custom mappers.
 */
@org.mapstruct.MapperConfig(builder = @Builder(disableBuilder = true))
public interface MapperConfig {
}
