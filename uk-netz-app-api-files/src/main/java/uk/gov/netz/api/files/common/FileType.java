package uk.gov.netz.api.files.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum FileType {

    DOCX("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    XLSX("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    XLS("xls", Set.of("application/vnd.ms-excel")),
    PDF("pdf", Set.of("application/pdf")),
    BMP("bmp", Set.of("image/bmp", "image/x-ms-bmp")),
    ZIP("zip", Set.of("application/zip"));
    
    private final String simpleType;
    private final Set<String> mimeTypes;
    
}
