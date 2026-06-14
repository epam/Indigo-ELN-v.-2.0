package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.RoleDTO;
import com.epam.indigoeln.eln.model.RoleEditRequest;
import com.epam.indigoeln.eln.model.RoleRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class RoleServiceTest extends ELNBaseTest {

    RoleDTO role;

    @Test
    @Order(1)
    void testCreateRole() {
        role = roleClient.createRole(new RoleRequest("testRole"));
        assertThat(role.getName()).isEqualTo("testRole");
    }

    @Test
    @Order(2)
    void testEditRole() {
        var updatedRole = roleClient.updateRole(role.getId(), new RoleEditRequest(
                JsonNullable.of("testRoleUpdated"),
                JsonNullable.of(Set.of(ApplicationPermission.MANAGE_DICTIONARIES, ApplicationPermission.CREATE_NOTEBOOKS))
        ));
        assertThat(updatedRole.getName()).isEqualTo("testRoleUpdated");
        assertThat(updatedRole.getPermissions()).containsExactlyInAnyOrder(ApplicationPermission.MANAGE_DICTIONARIES, ApplicationPermission.CREATE_NOTEBOOKS);
    }

    @Test
    @Order(3)
    void testDeleteRole() {
        roleClient.deleteRole(role.getId());
        var roles = roleClient.getRoles();
        assertThat(roles).extracting(RoleDTO::getName).doesNotContain("testRole", "testRole2");
    }
}
