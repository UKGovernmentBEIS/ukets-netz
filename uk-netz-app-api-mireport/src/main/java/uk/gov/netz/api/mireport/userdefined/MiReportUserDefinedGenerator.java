package uk.gov.netz.api.mireport.userdefined;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.mireport.userdefined.custom.AnyUserInfoDTO;
import uk.gov.netz.api.mireport.userdefined.custom.CustomQueryUserAttributes;
import uk.gov.netz.api.userinfoapi.UserInfoApi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.netz.api.common.exception.ErrorCode.CUSTOM_REPORT_ERROR;

@Service
@RequiredArgsConstructor
@Log4j2
class MiReportUserDefinedGenerator {

    private final UserInfoApi userInfoApi;

    public MiReportUserDefinedResult generateMiReport(EntityManager entityManager, String sqlQuery) {
        try {
            Session session = entityManager.unwrap(Session.class);

            final List<Map<String, Object>> results = new ArrayList<>();
            final List<String> columnNames = new ArrayList<>();

            session.doWork(connection -> {
                try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
                    try (ResultSet rs = ps.executeQuery()) {

                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            columnNames.add(metaData.getColumnLabel(i));
                        }

                        while (rs.next()) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(columnNames.get(i - 1), rs.getObject(i));
                            }
                            results.add(row);
                        }
                    }
                }
            });


            // Inject user information
            if (columnNames.stream().anyMatch(CustomQueryUserAttributes.getAllPredicates().stream().reduce(x -> false, Predicate::or))) {

                Set<String> uniqueUserIds = new HashSet<>();
                results.forEach(row -> uniqueUserIds.addAll(collectUserIdsFromRow(row)));
                List<String> userIds = new ArrayList<>(uniqueUserIds);

                Map<String, Map<String, String>> usersInfo = getUserInfoByUserIds(userIds);

                results.forEach(row -> updateUserInfoInRow(row, usersInfo));

                Arrays.stream(CustomQueryUserAttributes.values()).forEach(attribute ->
                        columnNames.replaceAll(columnName -> columnName.replace(attribute.getKeyword(), "")));
            }

            return MiReportUserDefinedResult.builder()
                    .columnNames(columnNames)
                    .results(results).build();
        } catch (Exception ex) {
            log.error(ex);
            throw new BusinessException(CUSTOM_REPORT_ERROR);
        }
    }

    private List<String> collectUserIdsFromRow(Map<String, Object> row) {
        List<String> ids = new ArrayList<>();
        Arrays.stream(CustomQueryUserAttributes.values())
                .forEach(attribute -> ids.addAll(
                        row.entrySet().stream()
                                .filter(column -> (attribute.getPredicate().test(column.getKey())))
                                .map(column -> (String)column.getValue())
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                ));

        return ids;
    }

    private Map<String, Map<String, String>> getUserInfoByUserIds(List<String> userIds) {
        final ObjectMapper objectMapper = new ObjectMapper();

        return userInfoApi.getUsersWithAttributes(userIds, AnyUserInfoDTO.class)
                .stream()
                .map(e -> objectMapper.convertValue(e, new TypeReference<Map<String, String>>() {}))
                .collect(Collectors.toMap(e -> e.get("id"), e -> e));
    }

    private void updateUserInfoInRow(Map<String, Object> row, Map<String, Map<String, String>> usersInfo) {
        Map<String, Object> updatedValues = new HashMap<>();

        Arrays.stream(CustomQueryUserAttributes.values()).forEach(attribute -> {
            row.entrySet().stream()
                    .filter(column -> (attribute.getPredicate().test(column.getKey())))
                    .forEach(column -> {
                        Map<String, String> userInfo = usersInfo.get(column.getValue());
                        updatedValues.put(column.getKey().replace(attribute.getKeyword(), ""), userInfo != null ? userInfo.get(attribute.getAttribute()) : column.getValue());
                    });
            row.entrySet().removeIf(column -> (attribute.getPredicate().test(column.getKey())));
        });

        row.putAll(updatedValues);
    }
}