package uk.gov.netz.api.notification.template.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateDTO;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateInfoDTO;
import uk.gov.netz.api.notification.template.repository.NotificationTemplateRepository;
import uk.gov.netz.api.notification.template.transform.NotificationTemplateMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateQueryServiceTest {

    @InjectMocks
    private NotificationTemplateQueryService service;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;
    
    private static NotificationTemplateMapper notificationTemplateMapper = Mappers.getMapper(NotificationTemplateMapper.class);

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "notificationTemplateMapper", notificationTemplateMapper);
    }
    
    @Test
    void getNotificationTemplateById() {
        Long notificatioTemplateId = 1L;
        NotificationTemplate expectedNotificationTemplate = NotificationTemplate.builder()
                .id(notificatioTemplateId)
                .build();
        
        when(notificationTemplateRepository.findById(notificatioTemplateId)).thenReturn(Optional.of(expectedNotificationTemplate));
        
        NotificationTemplate actualNotificationTemplate = service.getNotificationTemplateById(notificatioTemplateId);
        
        assertThat(actualNotificationTemplate).isEqualTo(expectedNotificationTemplate);
        verify(notificationTemplateRepository, times(1)).findById(notificatioTemplateId);
    }
    
    @Test
    void getNotificationTemplateById_not_found() {
        Long notificatioTemplateId = 1L;
        
        when(notificationTemplateRepository.findById(notificatioTemplateId)).thenReturn(Optional.empty());
        
        BusinessException be = assertThrows(BusinessException.class,
                () -> service.getNotificationTemplateById(notificatioTemplateId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        
        verify(notificationTemplateRepository, times(1)).findById(notificatioTemplateId);
    }
    
    @Test
    void getNotificationTemplateInfoDTOById() {
    	Long notificatioTemplateId = 1L;
    	LocalDateTime lastUpdatedDate = LocalDateTime.now();
    	NotificationTemplate notificationTemplate = NotificationTemplate.builder()
                .id(1L)
                .name("UserAccountCreated")
                .workflow("workflow")
                .roleType("OPERATOR")
                .lastUpdatedDate(lastUpdatedDate)
                .build();
    	
    	when(notificationTemplateRepository.findById(notificatioTemplateId)).thenReturn(Optional.of(notificationTemplate));
    	
    	NotificationTemplateInfoDTO result = service.getNotificationTemplateInfoDTOById(notificatioTemplateId);
    	
		assertThat(result).isEqualTo(new NotificationTemplateInfoDTO(1L,
				"UserAccountCreated", RoleTypeConstants.OPERATOR, "workflow", lastUpdatedDate));
    	
    	verify(notificationTemplateRepository, times(1)).findById(notificatioTemplateId);
    }
    
    @Test
    void getManagedNotificationTemplateById() {
        Long notificationTemplateId = 1L;
        String notificationTemplateName = "INVITATION_TO_OPERATOR_ACCOUNT";
        String notificationTemplateSubject = "notification template subject";
        String notificationTemplateText = "notification template text";
        String workflow = " workflow";
        String eventTrigger = "event trigger the notification";
        NotificationTemplate notificationTemplate = NotificationTemplate.builder()
            .id(notificationTemplateId)
            .name(notificationTemplateName)
            .subject(notificationTemplateSubject)
            .text(notificationTemplateText)
            .workflow(workflow)
            .eventTrigger(eventTrigger)
            .build();
        NotificationTemplateDTO notificationTemplateDTO = NotificationTemplateDTO.builder()
            .id(notificationTemplateId)
            .name(notificationTemplateName)
            .subject(notificationTemplateSubject)
            .text(notificationTemplateText)
            .eventTrigger(eventTrigger)
            .workflow(workflow)
            .build();

        when(notificationTemplateRepository.findManagedNotificationTemplateByIdWithText(notificationTemplateId))
            .thenReturn(Optional.of(notificationTemplate));

        NotificationTemplateDTO result = service.getManagedNotificationTemplateById(notificationTemplateId);
        assertEquals(notificationTemplateDTO, result);

        verify(notificationTemplateRepository, times(1))
            .findManagedNotificationTemplateByIdWithText(notificationTemplateId);
    }

    @Test
    void getManagedNotificationTemplateById_not_found() {
        Long notificationTemplateId = 1L;

        when(notificationTemplateRepository.findManagedNotificationTemplateByIdWithText(notificationTemplateId))
            .thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
            service.getManagedNotificationTemplateById(notificationTemplateId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(notificationTemplateRepository, times(1))
            .findManagedNotificationTemplateByIdWithText(notificationTemplateId);
    }

    @Test
    void getNotificationTemplateCaById() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.WALES;
        Long notificatioTemplateId = 1L;
        NotificationTemplate expectedNotificationTemplate = NotificationTemplate.builder()
                .id(notificatioTemplateId)
                .competentAuthority(competentAuthority)
                .build();

        when(notificationTemplateRepository.findById(notificatioTemplateId)).thenReturn(Optional.of(expectedNotificationTemplate));

        assertEquals(competentAuthority, service.getNotificationTemplateCaById(notificatioTemplateId));
        verify(notificationTemplateRepository, times(1)).findById(notificatioTemplateId);
    }

    @Test
    void getNotificationTemplateCaById_not_found() {
        Long notificatioTemplateId = 1L;

        when(notificationTemplateRepository.findById(notificatioTemplateId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () -> service.getNotificationTemplateCaById(notificatioTemplateId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(notificationTemplateRepository, times(1)).findById(notificatioTemplateId);
    }
    
}
