package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyProfile {

    private String name;
    private String registrationNumber;
    private CompanyType companyType;
    private String status;
    private String jurisdiction;
    private CompanyAddress address;
    private List<SicCode> sicCodes;

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    @JsonGetter("registrationNumber")
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    @JsonGetter("companyType")
    public CompanyType getCompanyType() {
        return companyType;
    }

    @JsonGetter("address")
    public CompanyAddress getAddress() {
        return address;
    }

    @JsonGetter("sicCodes")
    public List<SicCode> getSicCodes() {
        return sicCodes;
    }

    @JsonGetter("status")
    public String getStatus() {
        return status;
    }

    @JsonGetter("jurisdiction")
    public String getJurisdiction() {
        return jurisdiction;
    }

    @JsonSetter("company_name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter("company_number")
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @JsonSetter("type")
    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }

    @JsonSetter("company_status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonSetter("jurisdiction")
    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    @JsonSetter("registered_office_address")
    public void setAddress(CompanyAddress address) {
        this.address = address;
    }

    @JsonSetter("sic_codes")
    public void setSicCodes(List<SicCode> sicCodes) {
        this.sicCodes = sicCodes;
    }
}
