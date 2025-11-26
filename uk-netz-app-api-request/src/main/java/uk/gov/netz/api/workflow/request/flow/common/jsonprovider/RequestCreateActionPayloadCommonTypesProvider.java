package uk.gov.netz.api.workflow.request.flow.common.jsonprovider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestCreateActionPayloadTypes;
import uk.gov.netz.api.workflow.request.flow.common.domain.ReportRelatedRequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestCreateActionEmptyPayload;

@Component
public class RequestCreateActionPayloadCommonTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
				new NamedType(ReportRelatedRequestCreateActionPayload.class, RequestCreateActionPayloadTypes.REPORT_RELATED_REQUEST_CREATE_ACTION_PAYLOAD),
				
				new NamedType(RequestCreateActionEmptyPayload.class, RequestCreateActionPayloadTypes.EMPTY_PAYLOAD)
				);
	}

}
