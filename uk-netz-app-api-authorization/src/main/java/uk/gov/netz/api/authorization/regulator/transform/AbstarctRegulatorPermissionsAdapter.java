package uk.gov.netz.api.authorization.regulator.transform;

import uk.gov.netz.api.authorization.core.domain.RolePermission;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroupLevel;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class AbstarctRegulatorPermissionsAdapter implements RegulatorPermissionsAdapter {
    public abstract Map<RegulatorPermissionGroupLevel, List<String>> getPermissionGroupLevelsConfig();

    public List<String> getPermissionsFromPermissionGroupLevels(
            Map<String, RegulatorPermissionLevel> permissionGroupLevels) {
        List<String> permissions = new ArrayList<>();

        permissionGroupLevels.forEach((group, level) ->
                Optional.ofNullable(getPermissionGroupLevelsConfig().get(new RegulatorPermissionGroupLevel(group, level)))
                        .ifPresent(permissions::addAll));

        return permissions;
    }

    public Map<String, RegulatorPermissionLevel> getPermissionGroupLevelsFromPermissions(
            List<String> permissions) {

        Map<String, RegulatorPermissionLevel> permissionGroupLevels = new LinkedHashMap<>();
        getPermissionGroupLevelsConfig().forEach((configGroupLevel, configPermissionList) -> {
            if (permissions.containsAll(configPermissionList) &&
                    isExistingLevelLessThanConfigLevel(permissionGroupLevels.get(configGroupLevel.getGroup()),
                            configGroupLevel)) {
                permissionGroupLevels.put(configGroupLevel.getGroup(), configGroupLevel.getLevel());
            }
        });

        return permissionGroupLevels;
    }

    @Override
    public Map<String, RegulatorPermissionLevel> getPermissionGroupLevelsFromRolePermissions(List<RolePermission> permissions) {
        return getPermissionGroupLevelsFromPermissions(
                permissions.stream().map(RolePermission::getPermission)
                        .collect(Collectors.toList()));
    }

    public Map<String, List<RegulatorPermissionLevel>> getPermissionGroupLevels() {
        return
                getPermissionGroupLevelsConfig().keySet().stream()
                        .collect(Collectors.groupingBy(
                                RegulatorPermissionGroupLevel::getGroup,
                                LinkedHashMap::new,
                                Collectors.mapping(RegulatorPermissionGroupLevel::getLevel, toList())));
    }

    private boolean isExistingLevelLessThanConfigLevel(
            RegulatorPermissionLevel existingLevel, RegulatorPermissionGroupLevel configGroupLevel) {
        if (existingLevel == null) {
            return true;
        }
        return existingLevel.isLessThan(configGroupLevel.getLevel());
    }

}
