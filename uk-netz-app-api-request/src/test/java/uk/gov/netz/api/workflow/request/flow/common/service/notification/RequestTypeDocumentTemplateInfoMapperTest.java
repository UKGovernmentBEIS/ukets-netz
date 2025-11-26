package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTypeDocumentTemplateInfoMapperTest {

    @Test
    void test() {
        assertThat(RequestTypeDocumentTemplateInfoMapper.getTemplateInfo("code")).isEqualTo("N/A");
    }

    @Test
    void add() {
        RequestTypeDocumentTemplateInfoMapper.add("key", "value");
        assertThat(RequestTypeDocumentTemplateInfoMapper.getTemplateInfo("key")).isEqualTo("value");
    }
}
