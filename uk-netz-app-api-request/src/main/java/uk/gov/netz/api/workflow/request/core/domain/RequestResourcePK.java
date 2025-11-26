package uk.gov.netz.api.workflow.request.core.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RequestResourcePK implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String resourceType;

    private Request request;
}
