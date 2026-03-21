# ──────────────────────────────────────────────────────────────────────────────
# Feature: User Authentication
# Module:  Login
# Author:  Nishant Jaiswal
# ──────────────────────────────────────────────────────────────────────────────

@login @regression
Feature: User Authentication

  Background:
    Given the user is on the login page

  @smoke @TC-101
  Scenario: Successful login with valid credentials
    When the user enters username "standard_user"
    And the user enters password "secret_sauce"
    And the user clicks the login button
    Then the user should be redirected to the dashboard
    And the welcome message should be displayed

  @smoke @TC-102
  Scenario: Login fails with invalid password
    When the user enters username "standard_user"
    And the user enters password "wrong_password"
    And the user clicks the login button
    Then an error message "Invalid credentials" should be displayed
    And the user should remain on the login page

  @TC-103
  Scenario: Login fails with blank username
    When the user enters username ""
    And the user enters password "secret_sauce"
    And the user clicks the login button
    Then an error message "Username is required" should be displayed

  @TC-104
  Scenario: Login fails with blank password
    When the user enters username "standard_user"
    And the user enters password ""
    And the user clicks the login button
    Then an error message "Password is required" should be displayed

  @TC-105
  Scenario: Login fails for locked out user
    When the user enters username "locked_out_user"
    And the user enters password "secret_sauce"
    And the user clicks the login button
    Then an error message "Sorry, this user has been locked out" should be displayed

  @TC-106 @JIRA-AUTH-201
  Scenario Outline: Login with multiple user roles
    When the user enters username "<username>"
    And the user enters password "<password>"
    And the user clicks the login button
    Then the user should be redirected to the dashboard
    And the user role displayed should be "<role>"

    Examples:
      | username        | password     | role    |
      | admin_user      | admin_pass   | Admin   |
      | standard_user   | secret_sauce | User    |
      | readonly_user   | read_pass    | Viewer  |

  @TC-107
  Scenario: Remember me persists session after browser refresh
    When the user enters username "standard_user"
    And the user enters password "secret_sauce"
    And the user checks remember me
    And the user clicks the login button
    Then the user should be redirected to the dashboard
    When the user refreshes the page
    Then the user should still be logged in

  @TC-108
  Scenario: User can log out successfully
    When the user enters username "standard_user"
    And the user enters password "secret_sauce"
    And the user clicks the login button
    Then the user should be redirected to the dashboard
    When the user clicks logout
    Then the user should be redirected to the login page
