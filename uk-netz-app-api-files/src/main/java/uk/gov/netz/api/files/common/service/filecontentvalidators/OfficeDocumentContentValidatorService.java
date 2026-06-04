package uk.gov.netz.api.files.common.service.filecontentvalidators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import java.io.ByteArrayInputStream;
import java.util.Set;


@Component
@Order(300)
@Validated
@RequiredArgsConstructor
public class OfficeDocumentContentValidatorService implements FileContentValidatorService {

    @Value("${file.validators.office.macros.enabled:true}")
    private boolean macrosValidationEnabled;

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;

        return Set.of(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.visio",
                "application/vnd.ms-visio.drawing"
        ).contains(mimeType.toLowerCase());
    }

    @Override
    public void validate(FileDTO fileDTO) {
        byte[] content = fileDTO.getFileContent();

        try {
            FileMagic magic = FileMagic.valueOf(content);

            if (magic == FileMagic.OOXML) {
                if (macrosValidationEnabled) {
                    try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(content))) {
                        for (PackagePart part : pkg.getParts()) {
                            if (part.getPartName().getName().toLowerCase().contains("vba")) {
                                throw new SecurityException("Macros detected in OOXML file.");
                            }
                        }
                    }
                }
            } else if (magic == FileMagic.OLE2) {
                if (macrosValidationEnabled) {
                    try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(content))) {
                        if (fs.getRoot().hasEntry("Macros") ||
                                fs.getRoot().hasEntry("_VBA_PROJECT_CUR") ||
                                fs.getRoot().hasEntry("VBA")) {
                            throw new SecurityException("Macros detected in legacy Office file.");
                        }
                    }
                }
            } else {
                throw new SecurityException("Invalid Office document structure.");
            }

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Office validation failed: " + e.getMessage(), e);
        }
    }
}
