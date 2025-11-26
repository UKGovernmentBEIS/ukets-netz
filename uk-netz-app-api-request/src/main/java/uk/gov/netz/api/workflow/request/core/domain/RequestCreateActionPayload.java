package uk.gov.netz.api.workflow.request.core.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "payloadType", visible = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class RequestCreateActionPayload {

    private String payloadType;
}
