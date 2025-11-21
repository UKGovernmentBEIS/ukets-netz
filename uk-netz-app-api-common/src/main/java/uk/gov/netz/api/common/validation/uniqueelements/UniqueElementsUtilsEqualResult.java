package uk.gov.netz.api.common.validation.uniqueelements;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Data
@Builder
@RequiredArgsConstructor
public class UniqueElementsUtilsEqualResult {
    private final Boolean result;
    private final List<String> violatedFields;
}
