package uk.gov.netz.api.mireport.userdefined;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class MiReportUserDefinedResults {

    private List<MiReportUserDefinedInfoDTO> queries;
    private Long total;

    public static MiReportUserDefinedResults emptyMiReportUserDefinedResults() {
        return MiReportUserDefinedResults.builder().queries(Collections.emptyList()).total(0L).build();
    }
}
