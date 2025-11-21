package uk.gov.netz.api.common.config.jackson;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import java.util.List;

public interface JsonSubTypesProvider {
	
    List<NamedType> getTypes();
    
}
