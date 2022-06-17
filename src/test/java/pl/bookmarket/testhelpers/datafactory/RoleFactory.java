package pl.bookmarket.testhelpers.datafactory;

import pl.bookmarket.model.Role;

public class RoleFactory {

    private static final Role userRole = new Role(1L, "USER");
    private static final Role adminRole = new Role(2L, "ADMIN");
    private static long id = 3L;

    public static Role getDefaultRole() {
        return userRole;
    }

    public static Role getAdminRole() {
        return adminRole;
    }

    public static Role createRole(String name) {
        return new Role(id++, name);
    }
}