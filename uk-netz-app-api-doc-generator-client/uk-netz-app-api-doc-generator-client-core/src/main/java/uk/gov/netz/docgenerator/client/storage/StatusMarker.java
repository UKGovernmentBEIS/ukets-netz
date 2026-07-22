package uk.gov.netz.docgenerator.client.storage;

import java.util.Arrays;
import java.util.Optional;

public enum StatusMarker {

    UPLOADED("uploaded"),
    SUBMITTED("submitted"),
    SUBMISSION_FAILED("submission_failed"),
    PROCESSING("processing");

    private final String objectName;

    StatusMarker(String objectName) {
        this.objectName = objectName;
    }

    public String objectName() {
        return objectName;
    }

    public static Optional<StatusMarker> fromObjectName(String objectName) {
        return Arrays.stream(values())
            .filter(marker -> marker.objectName.equals(objectName))
            .findFirst();
    }
}
