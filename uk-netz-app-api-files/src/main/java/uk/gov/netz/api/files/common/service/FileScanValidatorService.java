package uk.gov.netz.api.files.common.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.io.ByteArrayInputStream;

@Component
@Order(300)
@Validated
@RequiredArgsConstructor
public class FileScanValidatorService implements FileValidatorService {

    private final FileScanService fileScanService;

    @Override
    public void validate(@Valid FileDTO fileDTO) {
        fileScanService.scan(new ByteArrayInputStream(fileDTO.getFileContent()));
    }
}
