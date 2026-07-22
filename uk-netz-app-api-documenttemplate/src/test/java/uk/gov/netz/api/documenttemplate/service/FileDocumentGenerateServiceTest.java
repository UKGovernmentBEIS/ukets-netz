package uk.gov.netz.api.documenttemplate.service;

import fr.opensagres.xdocreport.template.freemarker.FreemarkerTemplateEngine;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.competentauthority.CompetentAuthorityDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.documenttemplate.config.TemplatesConfiguration;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateFileInfoDTO;
import uk.gov.netz.api.documenttemplate.domain.templateparams.AccountTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.CompetentAuthorityTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.SignatoryTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.WorkflowTemplateParams;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDocumentGenerateServiceTest {
    private static FreemarkerTemplateEngine freemarkerTemplateEngine;

    @Mock
    private DocumentGeneratorRemoteClientService documentGeneratorClientService;

    @BeforeAll
    static void init() {
    	TemplatesConfiguration customFreeMarkerConfiguration = new TemplatesConfiguration();
        freemarker.template.Configuration freemarkerConfig = customFreeMarkerConfiguration.templatesFreemarkerConfig();

        TemplatesConfiguration templatesConfiguration = new TemplatesConfiguration();
        freemarkerTemplateEngine = templatesConfiguration.freemarkerTemplateEngine(freemarkerConfig);
    }

    @Test
    void generateFileDocumentFromTemplate_rfi_template_no_process_required() throws Exception {
        String fileNameToGenerate = "fileNameToGenerate";
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(false)
                .convertRequired(true)
                .file(createFile(templateFilePath))
                .build();

        byte[] resultExpected = "some bytes".getBytes();
        when(documentGeneratorClientService.generateDocument(Mockito.any(byte[].class), Mockito.eq(fileNameToGenerate), Mockito.eq(false)))
                .thenReturn(resultExpected);

        byte[] resultActual = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine)
                .generateFileDocumentFromTemplate(templateFile, TemplateParams.builder().build(), fileNameToGenerate);

        assertThat(resultActual).isEqualTo(resultExpected);

        ArgumentCaptor<byte[]> postProcessedDocumentCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(documentGeneratorClientService, times(1)).generateDocument(postProcessedDocumentCaptor.capture(), eq(fileNameToGenerate), eq(false));
    }

    @Test
    void generateFileDocumentFromTemplate_rfi_template_no_process_no_convert_required() throws Exception {
        String fileNameToGenerate = "fileNameToGenerate";
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(false)
                .convertRequired(false)
                .file(createFile(templateFilePath))
                .build();

        byte[] resultActual = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine)
                .generateFileDocumentFromTemplate(templateFile, TemplateParams.builder().build(), fileNameToGenerate);

        assertThat(resultActual).isEqualTo(templateFile.getFile().getFileContent());
        verifyNoInteractions(documentGeneratorClientService);
    }

    @Test
    void generateFileDocumentFromTemplate_withNormalize() throws Exception {
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        String fileNameToGenerate = "fileNameToGenerate";
        String signatoryUser = "Signatory user full name";
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(true)
                .convertRequired(true)
                .file(createFile(templateFilePath))
                .build();

        Path signatureFilePath = Paths.get("src", "test", "resources", "files", "signatures", "signature_valid.bmp");
        FileDTO signatureFile = createFile(signatureFilePath);

        Map<String, Object> params = new HashMap<>();
        Date deadlineDate = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        params.put("deadline", deadlineDate);
        params.put("questions", List.of("question1", "question2"));

        TemplateParams templateParams = buildTemplateParams(ca, signatoryUser, signatureFile, params);

        byte[] resultExpected = "some bytes".getBytes();
        String asyncResultExpected = "jobId";
        Map<String, String> documentMetadata = Map.of("key1", "val1");
        when(documentGeneratorClientService.generateDocument(Mockito.any(byte[].class), Mockito.eq(fileNameToGenerate), Mockito.eq(true)))
                .thenReturn(resultExpected);
        when(documentGeneratorClientService.generateDocumentAsync(
                Mockito.any(byte[].class),
                Mockito.eq(documentMetadata),
                Mockito.eq(true),
                Mockito.eq(DocumentGenerationPriority.HIGH))).thenReturn(asyncResultExpected);

        FileDocumentGenerateService service = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine);
        byte[] resultActual = service.generateFileDocumentFromTemplate(templateFile, templateParams, fileNameToGenerate, true);
        String asyncResultActual = service.generateFileDocumentFromTemplateAsyncConvert(templateFile, templateParams, documentMetadata);

        assertThat(resultActual).isEqualTo(resultExpected);
        assertThat(asyncResultActual).isEqualTo(asyncResultExpected);

        ArgumentCaptor<byte[]> documentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(documentGeneratorClientService, times(1)).generateDocument(documentCaptor.capture(), eq(fileNameToGenerate), eq(true));
        ArgumentCaptor<byte[]> asyncDocumentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(documentGeneratorClientService, times(1)).generateDocumentAsync(
                asyncDocumentCaptor.capture(),
                eq(documentMetadata),
                eq(true),
                eq(DocumentGenerationPriority.HIGH));
        assertThat(docxEntryHashes(documentCaptor.getValue())).isEqualTo(docxEntryHashes(asyncDocumentCaptor.getValue()));
    }

    @Test
    void generateFileDocumentFromTemplate_rfi_template() throws Exception {
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        String fileNameToGenerate = "fileNameToGenerate";
        String signatoryUser = "Signatory user full name";
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(true)
                .convertRequired(true)
                .file(createFile(templateFilePath))
                .build();

        Path signatureFilePath = Paths.get("src", "test", "resources", "files", "signatures", "signature_valid.bmp");
        FileDTO signatureFile = createFile(signatureFilePath);

        Map<String, Object> params = new HashMap<>();
        Date deadlineDate = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        params.put("deadline", deadlineDate);
        params.put("questions", List.of("question1", "question2"));

        TemplateParams templateParams = buildTemplateParams(ca, signatoryUser, signatureFile, params);

        byte[] resultExpected = "some bytes".getBytes();
        when(documentGeneratorClientService.generateDocument(Mockito.any(byte[].class), Mockito.eq(fileNameToGenerate), Mockito.eq(false)))
                .thenReturn(resultExpected);

        byte[] resultActual = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine)
                .generateFileDocumentFromTemplate(templateFile, templateParams, fileNameToGenerate);

        assertThat(resultActual).isEqualTo(resultExpected);

        ArgumentCaptor<byte[]> postProcessedDocumentCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(documentGeneratorClientService, times(1)).generateDocument(postProcessedDocumentCaptor.capture(), eq(fileNameToGenerate), eq(false));

        byte[] postProcessedDocument = postProcessedDocumentCaptor.getValue();

        try (InputStream bais = new ByteArrayInputStream(postProcessedDocument);
             XWPFDocument document = new XWPFDocument(bais);
             XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document)) {
            final String docText = xwpfWordExtractor.getText();
            assertThat(docText).contains(templateParams.getPermitId());
            assertThat(docText).contains(templateParams.getCompetentAuthorityParams().getName());
            assertThat(docText).contains(templateParams.getSignatoryParams().getFullName());
            assertThat(docText).contains("question1");
            assertThat(docText).contains("question2");
            assertThat(docText).contains(new SimpleDateFormat("dd MMMM yyyy").format(deadlineDate));
        }
    }
    
    @Test
    void generateFileDocumentFromTemplateAsyncConvert() throws Exception {
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        String signatoryUser = "Signatory user full name";
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(true)
                .convertRequired(true)
                .file(createFile(templateFilePath))
                .build();

        Path signatureFilePath = Paths.get("src", "test", "resources", "files", "signatures", "signature_valid.bmp");
        FileDTO signatureFile = createFile(signatureFilePath);

        Map<String, Object> params = new HashMap<>();
        Date deadlineDate = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        params.put("deadline", deadlineDate);
        params.put("questions", List.of("question1", "question2"));

        TemplateParams templateParams = buildTemplateParams(ca, signatoryUser, signatureFile, params);

        String resultExpected = "jobId";
        Map<String, String> documentMetadata = Map.of(
                "key1", "val1"
        );

        when(documentGeneratorClientService.generateDocumentAsync(Mockito.any(byte[].class),
                Mockito.eq(documentMetadata), Mockito.eq(true), Mockito.eq(DocumentGenerationPriority.HIGH))).thenReturn(resultExpected);

        String resultActual = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine)
                .generateFileDocumentFromTemplateAsyncConvert(templateFile, templateParams, documentMetadata);

        assertThat(resultActual).isEqualTo(resultExpected);

        ArgumentCaptor<byte[]> processedDocumentCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(documentGeneratorClientService, times(1)).generateDocumentAsync(processedDocumentCaptor.capture(),
                eq(documentMetadata), eq(true), eq(DocumentGenerationPriority.HIGH));

        byte[] processedDocument = processedDocumentCaptor.getValue();

        try (InputStream bais = new ByteArrayInputStream(processedDocument);
             XWPFDocument document = new XWPFDocument(bais);
             XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document)) {
            final String docText = xwpfWordExtractor.getText();
            assertThat(docText).contains(templateParams.getPermitId());
            assertThat(docText).contains(templateParams.getCompetentAuthorityParams().getName());
            assertThat(docText).contains(templateParams.getSignatoryParams().getFullName());
            assertThat(docText).contains("question1");
            assertThat(docText).contains("question2");
            assertThat(docText).contains(new SimpleDateFormat("dd MMMM yyyy").format(deadlineDate));
        }
    }

    @Test
    void generateFileDocumentFromTemplateAsyncConvertNoProcessRequired() throws Exception {
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        FileDTO sourceFile = createFile(templateFilePath);
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(false)
                .convertRequired(true)
                .file(sourceFile)
                .build();
        Map<String, String> documentMetadata = Map.of("key1", "val1");
        String resultExpected = "jobId";

        when(documentGeneratorClientService.generateDocumentAsync(Mockito.any(byte[].class),
                Mockito.eq(documentMetadata), Mockito.eq(false), Mockito.eq(DocumentGenerationPriority.HIGH))).thenReturn(resultExpected);

        String resultActual = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine)
                .generateFileDocumentFromTemplateAsyncConvert(templateFile, TemplateParams.builder().build(), documentMetadata);

        assertThat(resultActual).isEqualTo(resultExpected);

        ArgumentCaptor<byte[]> documentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(documentGeneratorClientService, times(1)).generateDocumentAsync(
                documentCaptor.capture(),
                eq(documentMetadata),
                eq(false),
                eq(DocumentGenerationPriority.HIGH));
        assertThat(documentCaptor.getValue()).isEqualTo(sourceFile.getFileContent());
    }

    @Test
    void generateFileDocumentFromTemplateAsyncConvertWithPriority() throws Exception {
        Path templateFilePath = Paths.get("src", "test", "resources", "templates", "L025_P3_Request_for_further_information_notice_20130402.docx");
        FileDTO sourceFile = createFile(templateFilePath);
        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .processRequired(false)
                .convertRequired(true)
                .file(sourceFile)
                .build();
        Map<String, String> documentMetadata = Map.of("key1", "val1");
        String resultExpected = "jobId";

        when(documentGeneratorClientService.generateDocumentAsync(
                Mockito.any(byte[].class),
                Mockito.eq(documentMetadata),
                Mockito.eq(false),
                Mockito.eq(DocumentGenerationPriority.LOW))).thenReturn(resultExpected);

        String resultActual = new FileDocumentGenerateService(documentGeneratorClientService, freemarkerTemplateEngine)
                .generateFileDocumentFromTemplateAsyncConvert(
                        templateFile,
                        TemplateParams.builder().build(),
                        documentMetadata,
                        DocumentGenerationPriority.LOW);

        assertThat(resultActual).isEqualTo(resultExpected);

        ArgumentCaptor<byte[]> documentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(documentGeneratorClientService, times(1)).generateDocumentAsync(
                documentCaptor.capture(),
                eq(documentMetadata),
                eq(false),
                eq(DocumentGenerationPriority.LOW));
        assertThat(documentCaptor.getValue()).isEqualTo(sourceFile.getFileContent());
    }


    private TemplateParams buildTemplateParams(CompetentAuthorityEnum ca, String signatoryUser, FileDTO signatureFile,
                                               Map<String, Object> params) {
        CompetentAuthorityDTO caDto = CompetentAuthorityDTO.builder().id(ca).email("email").name("name").build();
        AccountTemplateParams accountParams = Mockito.mock(AccountTemplateParams.class);
        return TemplateParams.builder()
                .competentAuthorityParams(CompetentAuthorityTemplateParams.builder()
                        .competentAuthority(caDto)
                        .logo(CompetentAuthorityService.getCompetentAuthorityLogo(ca))
                        .build())
                .competentAuthorityCentralInfo("ca central info")
                .signatoryParams(SignatoryTemplateParams.builder()
                        .fullName(signatoryUser)
                        .signature(signatureFile.getFileContent())
                        .jobTitle("Project Manager")
                        .build())
                .accountParams(accountParams)
                .permitId("UK-E-IN-12345")
                .workflowParams(WorkflowTemplateParams.builder()
                        .requestId("123")
                        .requestType("PERMIT_VARIATION") //("PERMIT_ISSUANCE")
                        .requestTypeInfo("your permit variation")
                        .requestSubmissionDate(new Date())
                        .requestEndDate(LocalDateTime.of(1998, 1, 1, 1, 1))
                        .build())
                .params(params)
                .build();
    }

    private FileDTO createFile(Path sampleFilePath) throws IOException {
        byte[] bytes = Files.readAllBytes(sampleFilePath);
        return FileDTO.builder()
                .fileContent(bytes)
                .fileName(sampleFilePath.getFileName().toString())
                .fileSize(sampleFilePath.toFile().length())
                .fileType(MimeTypeUtils.detect(bytes, sampleFilePath.getFileName().toString()))
                .build();
    }

    private Map<String, Integer> docxEntryHashes(byte[] documentBytes) throws IOException {
        Map<String, Integer> entryHashes = new HashMap<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(documentBytes))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryHashes.put(entry.getName(), Arrays.hashCode(zipInputStream.readAllBytes()));
            }
        }

        return entryHashes;
    }

}
