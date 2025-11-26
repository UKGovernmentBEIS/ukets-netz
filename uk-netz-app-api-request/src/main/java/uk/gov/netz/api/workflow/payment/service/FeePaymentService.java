package uk.gov.netz.api.workflow.payment.service;

import uk.gov.netz.api.workflow.payment.domain.enumeration.FeeMethodType;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import java.math.BigDecimal;

public interface FeePaymentService {

    BigDecimal getAmount(Request request);

    FeeMethodType getFeeMethodType();
}
