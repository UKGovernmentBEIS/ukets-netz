package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.config.CompetentAuthorityProperties;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.DateService;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.documenttemplate.domain.templateparams.AccountTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.CompetentAuthorityTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.SignatoryTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.WorkflowTemplateParams;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.user.regulator.domain.RegulatorUserDTO;
import uk.gov.netz.api.user.regulator.service.RegulatorUserAuthService;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RequiredArgsConstructor
public abstract class DocumentTemplateCommonParamsAbstractProvider implements DocumentTemplateCommonParamsProvider {
    private final RegulatorUserAuthService regulatorUserAuthService;
    private final UserAuthService userAuthService;
    private final CompetentAuthorityProperties competentAuthorityProperties;
    private final DateService dateService;
    private final CompetentAuthorityService competentAuthorityService;

    public abstract String getPermitReferenceId(Long accountId);

    public abstract AccountTemplateParams getAccountTemplateParams(Long accountId);

    public TemplateParams constructCommonTemplateParams(final Request request,
                                                        final String signatory) {
        final Long accountId = request.getAccountId();
        final String permitReferenceId = getPermitReferenceId(accountId);

        // account params
        final AccountTemplateParams accountTemplateParams = getAccountTemplateParams(accountId);

        // CA params
        final CompetentAuthorityEnum competentAuthority = accountTemplateParams.getCompetentAuthority();
        final CompetentAuthorityTemplateParams competentAuthorityParams = CompetentAuthorityTemplateParams.builder()
            .competentAuthority(competentAuthorityService.getCompetentAuthorityDTO(competentAuthority))
            .logo(CompetentAuthorityService.getCompetentAuthorityLogo(competentAuthority))
            .build();

        // Signatory params
        final RegulatorUserDTO signatoryUser = regulatorUserAuthService.getUserById(signatory);
        final FileInfoDTO signatureInfo = signatoryUser.getSignature();
        if (signatureInfo == null) {
            throw new BusinessException(ErrorCode.USER_SIGNATURE_NOT_EXIST, signatory);
        }
        final FileDTO signatorySignature = userAuthService.getUserSignature(signatureInfo.getUuid())
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, signatureInfo.getUuid()));
        final SignatoryTemplateParams signatoryParams = SignatoryTemplateParams.builder()
            .fullName(signatoryUser.getFullName())
            .jobTitle(signatoryUser.getJobTitle())
            .signature(signatorySignature.getFileContent())
            .build();

        // workflow params
        // request end date is set when the request closes, so for the permit issuance flow it is null at this point
        final LocalDateTime requestEndDate = request.getEndDate() != null ? request.getEndDate() : dateService.getLocalDateTime();
        final Date requestSubmissionDate = request.getSubmissionDate() != null ?
            Date.from(request.getSubmissionDate().atZone(ZoneId.systemDefault()).toInstant()) :
            Date.from(dateService.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant());

        final WorkflowTemplateParams workflowParams = WorkflowTemplateParams.builder()
            .requestId(request.getId())
            .requestSubmissionDate(requestSubmissionDate)
            .requestEndDate(requestEndDate)
            .requestTypeInfo(RequestTypeDocumentTemplateInfoMapper.getTemplateInfo(request.getType().getCode()))
            .requestType(request.getType().getCode())
            .build();

        return TemplateParams.builder()
            .competentAuthorityParams(competentAuthorityParams)
            .competentAuthorityCentralInfo(competentAuthorityProperties.getCentralInfo())
            .signatoryParams(signatoryParams)
            .accountParams(accountTemplateParams)
            .permitId(permitReferenceId)
            .workflowParams(workflowParams)
            .build();
    }
}
