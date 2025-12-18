package uk.gov.netz.api.files.common.service;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class ZipFileExtractor {

    public void consumeZip(byte[] zipBytes, BiConsumer<ZipEntry, InputStream> zipEntryConsumer) throws IOException {
        try (ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while((entry = zipFile.getNextEntry()) != null){
                zipEntryConsumer.accept(entry, new FilterInputStream(zipFile) {
                    @Override
                    public void close() throws IOException {
                        zipFile.closeEntry();
                    }
                });
            }
        }
    }

    public List<String> extractZipFilenames(byte[] zipBytes) throws IOException {
        final List<String> fileNames = new ArrayList<>();

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
             ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                fileNames.add(entry.getName());
            }
        }

        return fileNames;
    }
}
