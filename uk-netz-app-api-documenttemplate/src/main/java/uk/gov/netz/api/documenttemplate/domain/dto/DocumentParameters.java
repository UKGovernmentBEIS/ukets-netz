package uk.gov.netz.api.documenttemplate.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentParameters {

    private String outputFilename;
    private boolean normalize;

}
