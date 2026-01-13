package uk.gov.netz.integration.model.metscontacts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetsContactsEvent implements Serializable {

    private String operatorId;
    private List<MetsContactsMessage> details;
}
