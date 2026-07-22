package uk.gov.netz.docgenerator.client;

import uk.gov.netz.docgenerator.client.model.ConversionEvent;

@FunctionalInterface
public interface ConversionResultHandler {

    void handle(ConversionEvent event);
}
