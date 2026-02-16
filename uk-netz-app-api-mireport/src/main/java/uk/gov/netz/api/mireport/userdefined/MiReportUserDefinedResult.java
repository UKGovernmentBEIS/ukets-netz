package uk.gov.netz.api.mireport.userdefined;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiReportUserDefinedResult {

	private List<String> columnNames;

	private List<Map<String, Object>> results;
	
}
