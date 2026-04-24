package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.CurrentUserDTO;
import com.epam.indigoeln.eln.model.UserRef;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
class UserServiceTest extends ELNBaseTest {

    @Inject
    UserService userService;

    @Test
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    void testGetCurrentUser() {
        CurrentUserDTO user = userService.getCurrentUserDTO();
        assertThat(user.getId()).isEqualTo(johnUserID);
        assertThat(user.getUsername()).isEqualTo(JOHN_USERNAME);
        assertThat(user.getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
//        assertThat(user.getPermissions()).containsExactlyInAnyOrder(ApplicationPermission.values()); // TODO enable after database drop
        assertThat(user.getPermissions()).contains(ApplicationPermission.MANAGE_USERS);
    }

    @Test
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    void testSuggestUsers() {
        List<UserRef> all = userClient.suggestUsers(null);
        assertThat(all).map(UserRef::getDisplayName).containsExactly(ADMIN_DISPLAY_NAME, BART_DISPLAY_NAME, JOHN_DISPLAY_NAME, LISA_DISPLAY_NAME, MAGGIE_DISPLAY_NAME, WILLOW_DISPLAY_NAME);
        List<UserRef> filtered = userClient.suggestUsers("l");
        assertThat(filtered).map(UserRef::getDisplayName).containsExactly(LISA_DISPLAY_NAME);
    }

    @Test
    @SneakyThrows
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    void testGetUserPicture() {
        List<UserRef> all = userClient.suggestUsers(null);
        byte[] response = userClient.getUserPicture(all.getFirst().getId(), null);
        Files.write(Paths.get("user.png"), response);
    }
}
