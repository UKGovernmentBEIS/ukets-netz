package uk.gov.netz.api.workflow.request.core.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class RequestMetadata {

    private String type;
    
}
