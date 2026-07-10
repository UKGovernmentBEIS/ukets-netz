package uk.gov.netz.api.alert.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.alert.dto.NotificationAlertDTO;
import uk.gov.netz.api.alert.mapper.NotificationAlertMapper;
import uk.gov.netz.api.alert.repository.NotificationAlertRepository;

@Service
@RequiredArgsConstructor
public class NotificationAlertService {

	private final NotificationAlertRepository notificationAlertRepository;
	private final NotificationAlertMapper mapper;

    public List<NotificationAlertDTO> getNotificationAlerts() {
    	LocalDateTime currentDate = LocalDateTime.now();
        return mapper.toNotificationAlertDTO(
        		notificationAlertRepository.findAllByOrderByActiveFromAsc().stream()
        		.filter(alert -> currentDate.isAfter(alert.getActiveFrom()) && currentDate.isBefore(alert.getActiveUntil()))
        		.toList());	
    }
}
