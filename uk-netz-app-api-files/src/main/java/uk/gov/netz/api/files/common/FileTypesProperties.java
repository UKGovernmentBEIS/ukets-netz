package uk.gov.netz.api.files.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "files")
@Data
public class FileTypesProperties {

    private List<String> allowedMimeTypes = new ArrayList<>();
    
    private Zip zip;

    @Getter
    @Setter
    public static class Zip {
    	
    	private long extractedMaxSizeMb; // megabytes
    	
    	private List<String> allowedMimeTypes = new ArrayList<>();
        
        public Long getExtractedMaxSizeInBytes() {
        	return extractedMaxSizeMb * 1024 * 1024;
        }
        
    }
    
    
}
