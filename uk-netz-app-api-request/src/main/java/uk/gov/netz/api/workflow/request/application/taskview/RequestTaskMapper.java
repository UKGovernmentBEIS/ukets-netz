package uk.gov.netz.api.workflow.request.application.taskview;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface RequestTaskMapper {

	@Mapping(target = "type", source = "requestTask.type.code")
    @Mapping(target = "assigneeUserId", source = "assignee")
    @Mapping(target = "assignable", source = "requestTask.type.assignable")
    @Mapping(target = "daysRemaining", expression = "java(uk.gov.netz.api.common.utils.DateUtils.getDaysRemaining(requestTask.getPauseDate(), requestTask.getDueDate()))")
    RequestTaskDTO toTaskDTO(RequestTask requestTask, @Context UserDTO assigneeUser);

    @AfterMapping
    default void populateAssigneeUser(RequestTask requestTask, @MappingTarget RequestTaskDTO taskDTO, @Context UserDTO assigneeUser) {
        if (assigneeUser != null) {
            taskDTO.setAssigneeFullName(assigneeUser.getFullName());
        }
    }
}
