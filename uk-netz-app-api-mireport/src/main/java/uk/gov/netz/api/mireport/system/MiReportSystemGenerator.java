package uk.gov.netz.api.mireport.system;

import jakarta.persistence.EntityManager;

public interface MiReportSystemGenerator<T extends MiReportSystemParams> {

    MiReportSystemResult generateMiReport(EntityManager entityManager, T reportParams);

    String getReportType();

}
