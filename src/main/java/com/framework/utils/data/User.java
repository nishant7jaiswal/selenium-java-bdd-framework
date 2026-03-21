package com.framework.utils.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User POJO for JSON test data binding.
 *
 * Usage:
 *   User user = JsonUtils.readFromClasspath("test-data/json/users.json", User.class);
 *   loginPage.loginAs(user.getUsername(), user.getPassword());
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("email")
    private String email;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("role")
    private String role;

    @JsonProperty("active")
    private boolean active;

    // ── Factory Methods ──────────────────────────────────────────────────

    /**
     * Generate a random user for test isolation.
     */
    public static User random() {
        return User.builder()
            .username(TestDataGenerator.username())
            .password(TestDataGenerator.password())
            .email(TestDataGenerator.uniqueEmail())
            .firstName(TestDataGenerator.firstName())
            .lastName(TestDataGenerator.lastName())
            .role("USER")
            .active(true)
            .build();
    }

    public static User admin() {
        return User.builder()
            .username("admin_" + TestDataGenerator.shortUuid())
            .password(TestDataGenerator.password())
            .email(TestDataGenerator.uniqueEmail())
            .firstName(TestDataGenerator.firstName())
            .lastName(TestDataGenerator.lastName())
            .role("ADMIN")
            .active(true)
            .build();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
