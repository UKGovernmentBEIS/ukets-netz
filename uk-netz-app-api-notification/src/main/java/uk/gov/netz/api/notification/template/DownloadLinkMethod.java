package uk.gov.netz.api.notification.template;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;

import java.util.List;

/**
 * Renders one Markdown link for the existing FreeMarker, HTML-escaping, and Markdown pipeline.
 */
public final class DownloadLinkMethod implements TemplateMethodModelEx {

    @Override
    @SuppressWarnings("rawtypes")
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.isEmpty() || arguments.size() > 2
                || !(DeepUnwrap.unwrap((TemplateModel) arguments.getFirst()) instanceof EmailFileLink file)) {
            throw new TemplateModelException("downloadLink expects an EmailFileLink and an optional text label");
        }
        Object label = arguments.size() == 2
                ? DeepUnwrap.unwrap((TemplateModel) arguments.get(1)) : file.getFileName();
        if (!(label instanceof String text) || file.getUrl() == null) {
            throw new TemplateModelException("downloadLink requires a text label and a file URL");
        }
        String destination = file.getUrl().toASCIIString().replace("(", "%28").replace(")", "%29");
        return "[" + escapeLabel(text) + "](" + destination + ")";
    }

    private String escapeLabel(String label) {
        StringBuilder escaped = new StringBuilder();
        for (char character : label.toCharArray()) {
            if ("\\[]`*_!".indexOf(character) >= 0) {
                escaped.append('\\');
            }
            escaped.append(character == '\r' || character == '\n' || character == '\t' ? ' ' : character);
        }
        return escaped.toString();
    }
}
