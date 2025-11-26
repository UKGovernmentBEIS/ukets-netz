package uk.gov.netz.api.authorization.regulator.transform;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroupLevel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_CA_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_TASK_ASSIGNMENT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_VB_MANAGE;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.ADD_OPERATOR_ADMIN;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.ASSIGN_REASSIGN_TASKS;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.MANAGE_USERS_AND_CONTACTS;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.MANAGE_VERIFICATION_BODIES;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel.EXECUTE;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel.NONE;

public class TestRegulatorPermissionsAdapter extends AbstarctRegulatorPermissionsAdapter implements InitializingBean {
    private final Map<RegulatorPermissionGroupLevel, List<String>> permissionGroupLevelsConfig = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() {
        //MANAGE_USERS_AND_CONTACTS
        permissionGroupLevelsConfig
            .put(new RegulatorPermissionGroupLevel(MANAGE_USERS_AND_CONTACTS, NONE),
                Collections.emptyList());
        permissionGroupLevelsConfig
            .put(new RegulatorPermissionGroupLevel(MANAGE_USERS_AND_CONTACTS, EXECUTE),
                List.of(PERM_CA_USERS_EDIT));

        //ADD_OPERATOR_ADMIN
        permissionGroupLevelsConfig
                .put(new RegulatorPermissionGroupLevel(ADD_OPERATOR_ADMIN, NONE), List.of());
        permissionGroupLevelsConfig
                .put(new RegulatorPermissionGroupLevel(ADD_OPERATOR_ADMIN, EXECUTE),
                        List.of(PERM_ACCOUNT_USERS_EDIT));

        //ASSIGN_REASSIGN TASKS
        permissionGroupLevelsConfig
            .put(new RegulatorPermissionGroupLevel(ASSIGN_REASSIGN_TASKS, NONE), List.of());
        permissionGroupLevelsConfig
            .put(new RegulatorPermissionGroupLevel(ASSIGN_REASSIGN_TASKS, EXECUTE),
                List.of(PERM_TASK_ASSIGNMENT));

        //MANAGE_VERIFICATION_BODIES
        permissionGroupLevelsConfig
                .put(new RegulatorPermissionGroupLevel(MANAGE_VERIFICATION_BODIES, NONE), List.of());
        permissionGroupLevelsConfig
                .put(new RegulatorPermissionGroupLevel(MANAGE_VERIFICATION_BODIES, EXECUTE),
                        List.of(PERM_VB_MANAGE));

    }


    @Override
    public Map<RegulatorPermissionGroupLevel, List<String>> getPermissionGroupLevelsConfig() {
        return permissionGroupLevelsConfig;
    }
}
