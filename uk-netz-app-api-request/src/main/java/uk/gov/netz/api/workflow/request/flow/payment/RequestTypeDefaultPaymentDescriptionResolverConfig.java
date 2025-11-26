package uk.gov.netz.api.workflow.request.flow.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RequestTypeDefaultPaymentDescriptionResolverConfig {

    @Bean
    @ConditionalOnMissingBean
    public RequestTypePaymentDescriptionResolver produceDefault() {
        return new RequestTypeDefaultPaymentDescriptionResolver();
    }
}
