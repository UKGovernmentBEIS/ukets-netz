package uk.gov.netz.api.referencedata.domain.constants;

import java.util.Set;

public final class CountryConstants {

	public static final String ENGLAND_CODE = "GB-ENG";
	public static final String NORTHERN_IRELAND_CODE = "GB-NIR";
	public static final String SCOTLAND_CODE = "GB-SCT";
	public static final String WALES_CODE = "GB-WLS";

	private CountryConstants() {}

	public static Set<String> getUKCountryCodes() {
		return Set.of(ENGLAND_CODE, NORTHERN_IRELAND_CODE, SCOTLAND_CODE, WALES_CODE);
	}
}
