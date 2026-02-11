package uk.gov.netz.integration.model.regulatornotice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegulatorNoticeEvent implements Serializable {

    private String registryId;
    private String type;
    private byte[] fileData;
    private String fileName;
}
