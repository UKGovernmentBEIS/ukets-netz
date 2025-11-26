package uk.gov.netz.api.workflow.request.flow.common.service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

public interface CalculateApplicationReviewExpirationDateService {

    Optional<Date> expirationDate();

    Set<String> getTypes();
}
