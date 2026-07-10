package uk.gov.netz.api.alert.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import uk.gov.netz.api.alert.domain.NotificationAlert;
import uk.gov.netz.api.alert.dto.NotificationAlertDTO;
import uk.gov.netz.api.common.config.MapperConfig;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface NotificationAlertMapper {

	List<NotificationAlertDTO> toNotificationAlertDTO(List<NotificationAlert> notificationAlerts);
}
