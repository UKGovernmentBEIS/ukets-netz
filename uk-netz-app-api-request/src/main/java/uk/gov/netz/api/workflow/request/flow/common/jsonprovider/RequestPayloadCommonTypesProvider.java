package uk.gov.netz.api.workflow.request.flow.common.jsonprovider;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestPayloadTypes;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestPayload;

import java.util.List;

@Component
public class RequestPayloadCommonTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
			new NamedType(SystemMessageNotificationRequestPayload.class, RequestPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_REQUEST_PAYLOAD)
		);
	}

}
