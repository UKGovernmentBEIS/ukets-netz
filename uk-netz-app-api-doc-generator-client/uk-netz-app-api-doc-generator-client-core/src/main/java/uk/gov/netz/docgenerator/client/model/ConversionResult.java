package uk.gov.netz.docgenerator.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface ConversionResult {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Success implements ConversionResult {

        private String jobId;
        private String outputObjectKey;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Failed implements ConversionResult {

        private String jobId;
        private String errorReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Timeout implements ConversionResult {

        private String jobId;
    }
}
