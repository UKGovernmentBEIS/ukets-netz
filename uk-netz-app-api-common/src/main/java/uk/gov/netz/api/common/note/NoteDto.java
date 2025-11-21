package uk.gov.netz.api.common.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NoteDto {

    private Long id;
    private NotePayload payload;
    private String submitter;
    private LocalDateTime lastUpdatedOn;
}
