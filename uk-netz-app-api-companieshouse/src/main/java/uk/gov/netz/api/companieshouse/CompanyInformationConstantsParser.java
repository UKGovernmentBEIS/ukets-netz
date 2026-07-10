package uk.gov.netz.api.companieshouse;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.InputStreamReader;
import java.util.Map;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

public class CompanyInformationConstantsParser {

    private static final String constantsFilePath = "constants.yml";

    static {
        Map<String, Map<String, String>> constants = parseFile();
        companyTypes = constants.get("company_type");
        sicDescriptions = constants.get("sic_descriptions");
    }

    @Getter
    private static final Map<String, String> companyTypes;

    @Getter
    private static final Map<String, String> sicDescriptions;

    private static Map<String, Map<String, String>> parseFile(){
        try {
            InputStreamReader isr = new InputStreamReader(new ClassPathResource(constantsFilePath).getInputStream());
            YamlReader reader = new YamlReader(isr);
            return (Map<String, Map<String, String>>) reader.read();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR_CH_API);
        }
    }
}
