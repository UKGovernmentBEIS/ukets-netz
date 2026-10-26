package uk.gov.netz.api.request.feedback.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackDetailsDTO;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface RequestFeedbackMapper {

    RequestFeedbackDetailsDTO mapToDTO(RequestFeedback requestFeedback, boolean expired);
}
