package uk.gov.netz.api.authorization.regulator.transform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_CA_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_TASK_ASSIGNMENT;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.ADD_OPERATOR_ADMIN;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.ASSIGN_REASSIGN_TASKS;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.MANAGE_USERS_AND_CONTACTS;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel.EXECUTE;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel.NONE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class RegulatorPermissionsAdapterTest {

    private TestRegulatorPermissionsAdapter regulatorPermissionsAdapter = new TestRegulatorPermissionsAdapter();

    @BeforeAll
    void beforeAll() {
        regulatorPermissionsAdapter = new TestRegulatorPermissionsAdapter();
        regulatorPermissionsAdapter.afterPropertiesSet();
    }

    @Test
    void getPermissionsFromPermissionGroupLevels_one_permission_per_group_level() {
        Map<String, RegulatorPermissionLevel> permissionGroupLevels =
                Map.of(MANAGE_USERS_AND_CONTACTS, NONE,
                        ADD_OPERATOR_ADMIN, NONE,
                        ASSIGN_REASSIGN_TASKS, EXECUTE);

        List<String> expectedPermissions = List.of(
                PERM_TASK_ASSIGNMENT);

        assertThat(regulatorPermissionsAdapter.getPermissionsFromPermissionGroupLevels(permissionGroupLevels))
                .containsExactlyInAnyOrderElementsOf(expectedPermissions);
    }

    @Test
    void getPermissionsFromPermissionGroupLevels_multiple_permissions_per_group_level() {
        Map<String, RegulatorPermissionLevel> permissionGroupLevels =
                Map.of(MANAGE_USERS_AND_CONTACTS, NONE,
                        ADD_OPERATOR_ADMIN, NONE,
                        ASSIGN_REASSIGN_TASKS, EXECUTE);

        List<String> expectedPermissions = List.of(
                PERM_TASK_ASSIGNMENT);

        assertThat(regulatorPermissionsAdapter.getPermissionsFromPermissionGroupLevels(permissionGroupLevels))
                .containsExactlyInAnyOrderElementsOf(expectedPermissions);
    }

    @Test
    void getPermissionsFromPermissionGroupLevels_multiple_permissions() {
        Map<String, RegulatorPermissionLevel> permissionGroupLevels = Map.ofEntries(
                Map.entry(MANAGE_USERS_AND_CONTACTS, EXECUTE),
                Map.entry(ADD_OPERATOR_ADMIN, EXECUTE),
                Map.entry(ASSIGN_REASSIGN_TASKS, EXECUTE));

        List<String> expectedPermissions = List.of(
                PERM_ACCOUNT_USERS_EDIT,
                PERM_CA_USERS_EDIT,
                PERM_TASK_ASSIGNMENT);

        assertThat(regulatorPermissionsAdapter.getPermissionsFromPermissionGroupLevels(permissionGroupLevels))
                .containsExactlyInAnyOrderElementsOf(expectedPermissions);
    }

    @Test
    void getPermissionGroupLevelsFromPermissions_one_permission_per_group_level() {
        Map<String, RegulatorPermissionLevel> permissionGroupLevels =
                Map.of(MANAGE_USERS_AND_CONTACTS, NONE,
                        ASSIGN_REASSIGN_TASKS, EXECUTE);

        List<String> expectedPermissions = List.of(
                PERM_TASK_ASSIGNMENT);

        assertThat(regulatorPermissionsAdapter.getPermissionsFromPermissionGroupLevels(permissionGroupLevels))
                .containsExactlyInAnyOrderElementsOf(expectedPermissions);
    }

    @Test
    void getPermissionGroupLevelsFromPermissions_multiple_permissions_per_group_level() {
        List<String> permissions = List.of(
                PERM_ACCOUNT_USERS_EDIT,
                PERM_TASK_ASSIGNMENT);

        Map<String, RegulatorPermissionLevel> expectedPermissionGroupLevels = new LinkedHashMap<>();
        expectedPermissionGroupLevels.put(MANAGE_USERS_AND_CONTACTS, NONE);
        expectedPermissionGroupLevels.put(ASSIGN_REASSIGN_TASKS, EXECUTE);
        expectedPermissionGroupLevels.put(ADD_OPERATOR_ADMIN, EXECUTE);
        expectedPermissionGroupLevels.put(RegulatorPermissionGroup.MANAGE_VERIFICATION_BODIES, NONE);

        assertThat(regulatorPermissionsAdapter.getPermissionGroupLevelsFromPermissions(permissions))
                .containsExactlyInAnyOrderEntriesOf(expectedPermissionGroupLevels);
    }

    @Test
    void getPermissionGroupLevelsFromPermissions() {
        List<String> permissions = List.of(
                PERM_ACCOUNT_USERS_EDIT,
                PERM_TASK_ASSIGNMENT,
                PERM_CA_USERS_EDIT,
                Permission.PERM_VB_MANAGE);

        Map<String, RegulatorPermissionLevel> expectedPermissionGroupLevels = new LinkedHashMap<>();
        expectedPermissionGroupLevels.put(MANAGE_USERS_AND_CONTACTS, EXECUTE);
        expectedPermissionGroupLevels.put(ASSIGN_REASSIGN_TASKS, EXECUTE);
        expectedPermissionGroupLevels.put(ADD_OPERATOR_ADMIN, EXECUTE);
        expectedPermissionGroupLevels.put(RegulatorPermissionGroup.MANAGE_VERIFICATION_BODIES, EXECUTE);

        assertThat(regulatorPermissionsAdapter.getPermissionGroupLevelsFromPermissions(permissions))
                .containsExactlyInAnyOrderEntriesOf(expectedPermissionGroupLevels);
    }

    @Test
    void getPermissionGroupLevels() {
        Map<String, List<RegulatorPermissionLevel>> expectedPermissionGroupLevels = new LinkedHashMap<>();
        expectedPermissionGroupLevels.put(MANAGE_USERS_AND_CONTACTS, List.of(NONE, EXECUTE));
        expectedPermissionGroupLevels.put(ASSIGN_REASSIGN_TASKS, List.of(NONE, EXECUTE));
        expectedPermissionGroupLevels.put(ADD_OPERATOR_ADMIN, List.of(NONE, EXECUTE));
        expectedPermissionGroupLevels.put(RegulatorPermissionGroup.MANAGE_VERIFICATION_BODIES, List.of(NONE, EXECUTE));

        Map<String, List<RegulatorPermissionLevel>> actualPermissionGroupLevels =
                regulatorPermissionsAdapter.getPermissionGroupLevels();

        assertThat(actualPermissionGroupLevels.keySet())
                .containsExactlyInAnyOrderElementsOf(expectedPermissionGroupLevels.keySet());
        actualPermissionGroupLevels.forEach((group, levels) ->
                assertThat(levels).containsExactlyElementsOf(expectedPermissionGroupLevels.get(group)));
    }

}