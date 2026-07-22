package uk.gov.netz.docgenerator.client.storage;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutputObject {

    private String objectKey;
    private Instant lastModified;
}
