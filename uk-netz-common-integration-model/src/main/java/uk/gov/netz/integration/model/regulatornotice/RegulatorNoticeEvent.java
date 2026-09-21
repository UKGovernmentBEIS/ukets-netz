package uk.gov.netz.integration.model.regulatornotice;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "regulatorNoticeEventType",
        visible = true,
        defaultImpl = RegulatorNoticeEvent.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReturnOfAllowancesRegulatorNoticeEvent.class, name = "RETURN_OF_ALLOWANCES")
})
public class RegulatorNoticeEvent implements Serializable {

    private String registryId;
    private String type;
    private byte[] fileData;
    private String fileName;
    private RegulatorNoticeEventType regulatorNoticeEventType;
}
