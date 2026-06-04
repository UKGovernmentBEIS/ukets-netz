package uk.gov.netz.api.files.common.service.filecontentvalidators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;


import java.io.IOException;

@Component
@Order(300)
@Validated
@RequiredArgsConstructor
public class PdfContentValidatorService implements FileContentValidatorService{

    @Value("${file.validators.pdf.max-pages:1000}")
    private int maxPages;

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;

        return "application/pdf".equalsIgnoreCase(mimeType);
    }

    @Override
    public void validate(FileDTO fileDTO) {
        try (PDDocument document = Loader.loadPDF(fileDTO.getFileContent())) {

            int pageCount = document.getNumberOfPages();

            if (pageCount < 1) {
                throw new SecurityException("Invalid PDF: No pages found.");
            }

            if (pageCount > maxPages) {
                throw new SecurityException("PDF exceeds maximum allowed pages: " + maxPages);
            }

            if (document.isEncrypted()) {
                throw new SecurityException("Rejected: Encrypted PDFs are not allowed.");
            }

        } catch (IOException e) {
            throw new SecurityException("PDF structural validation failed.", e);
        }
    }
}
