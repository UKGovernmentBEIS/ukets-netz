package uk.gov.netz.api.notification.template.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateDTO;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateInfoDTO;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface NotificationTemplateMapper {

    NotificationTemplateDTO toNotificationTemplateDTO(NotificationTemplate notificationTemplate);

    NotificationTemplateInfoDTO toNotificationTemplateInfoDTO(NotificationTemplate notificationTemplate);
}
