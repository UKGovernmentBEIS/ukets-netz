package uk.gov.netz.api.documenttemplate.initialdata;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.netz.api.common.domain.ResourceFile;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.files.common.utils.ResourceFileUtils;

import java.io.File;
import java.io.IOException;

@Getter
@Setter
public abstract class DocumentTemplateFileUploadTaskChange implements CustomTaskChange {

    private CompetentAuthorityEnum competentAuthority;
    private String fileDocumentName;

    @Override
    public String getConfirmationMessage() {
        return "File document upload successfully completed";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    protected ResourceFile findCaTemplateResourceFile()
        throws CustomChangeException {
        String resourcePath = "templates" + File.separator + "ca" + File.separator + competentAuthority.name().toLowerCase() + File.separator + fileDocumentName;
        try {
            return ResourceFileUtils.getResourceFile(resourcePath);
        } catch (IOException e) {
            throw new CustomChangeException(e.getMessage());
        }
    }
}
