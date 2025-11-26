package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class RequestTypeDocumentTemplateInfoMapper {

    private static Map<String, String> map = new HashMap<>();
    
    public String getTemplateInfo(String requestType) {
        return map.getOrDefault(requestType, "N/A");
    }

    public static void add(String requestType, String info) {
        map.put(requestType, info);
    }
}
