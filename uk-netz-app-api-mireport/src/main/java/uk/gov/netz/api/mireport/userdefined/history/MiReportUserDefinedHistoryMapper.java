package uk.gov.netz.api.mireport.userdefined.history;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface MiReportUserDefinedHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "miReportId", source = "source.id")
    @Mapping(target = "categories", source = "source.categories", qualifiedByName = "categoryNames")
    MiReportUserDefinedHistoryEntity toMiReportUserDefinedHistoryEntity(
            MiReportUserDefinedEntity source,
            MiReportUserDefinedChangeType changeType,
            String submittedBy,
            String reasonForChange);

    MiReportUserDefinedHistoryDTO toMiReportUserDefinedHistoryDTO(MiReportUserDefinedHistoryEntity entity);

    @Named("categoryNames")
    default String toCategoryNames(Set<MiReportUserDefinedCategoryEntity> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.stream()
                .map(MiReportUserDefinedCategoryEntity::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

}
