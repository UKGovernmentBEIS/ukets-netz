package uk.gov.netz.api.common.config;


import com.fasterxml.jackson.databind.ObjectMapper;

import io.hypersistence.utils.hibernate.type.util.ObjectMapperSupplier;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;

public class HibernateTypesObjectMapperSupplier implements ObjectMapperSupplier {

    @Override
    public ObjectMapper get() {
    	return ObjectMapperWrapper.INSTANCE.getObjectMapper();
    }
}
