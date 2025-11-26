package uk.gov.netz.api.workflow.request.application.item.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RequestTaskVisitPK implements Serializable {

    private static final long serialVersionUID = 1L;

	private Long taskId;

    private String userId;
}
