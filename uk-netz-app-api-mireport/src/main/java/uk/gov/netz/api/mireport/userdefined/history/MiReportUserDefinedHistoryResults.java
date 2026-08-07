package uk.gov.netz.api.mireport.userdefined.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MiReportUserDefinedHistoryResults {

    private List<MiReportUserDefinedHistoryDTO> results;
    private Long total;

    public static MiReportUserDefinedHistoryResults emptyMiReportUserDefinedHistoryResults() {
        return MiReportUserDefinedHistoryResults.builder().results(Collections.emptyList()).total(0L).build();
    }
}
