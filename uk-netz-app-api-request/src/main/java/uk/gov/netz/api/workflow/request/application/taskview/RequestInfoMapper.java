package uk.gov.netz.api.workflow.request.application.taskview;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.workflow.request.core.domain.Request;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface RequestInfoMapper {

	@Mapping(target = "type", source = "type.code")
    @Mapping(target = "requestMetadata", source = "metadata")
    @Mapping(target = "paymentCompleted", source = "payload.paymentCompleted")
    @Mapping(target = "paymentAmount", source = "payload.paymentAmount")
    @Mapping(target = "resources", source = "requestResourcesMap")
    @Mapping(target = "resourceType", source = "type.resourceType")
    RequestInfoDTO toRequestInfoDTO(Request request);

}
