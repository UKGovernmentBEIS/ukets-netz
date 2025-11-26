package uk.gov.netz.api.authorization.regulator.transform;

import org.mapstruct.Named;
import uk.gov.netz.api.authorization.core.domain.RolePermission;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel;

import java.util.List;
import java.util.Map;

public interface RegulatorPermissionsAdapter {

    List<String> getPermissionsFromPermissionGroupLevels(Map<String, RegulatorPermissionLevel> permissionGroupLevels);

    Map<String, RegulatorPermissionLevel> getPermissionGroupLevelsFromPermissions(List<String> permissions);
    @Named("getPermissionGroupLevelsFromRolePermissions")
    Map<String, RegulatorPermissionLevel> getPermissionGroupLevelsFromRolePermissions(List<RolePermission> permissions) ;


    Map<String, List<RegulatorPermissionLevel>> getPermissionGroupLevels();


}
