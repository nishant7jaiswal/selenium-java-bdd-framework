# ──────────────────────────────────────────────────────────────────────────────
# Feature: Dashboard
# Module:  Dashboard
# Author:  Nishant Jaiswal
# ──────────────────────────────────────────────────────────────────────────────

@dashboard @regression
Feature: Dashboard Functionality

  Background:
    Given the user is logged in as "standard_user"

  @smoke @TC-201
  Scenario: Dashboard loads with correct welcome message
    Then the dashboard should be displayed
    And the welcome message should contain the username

  @TC-202
  Scenario: Navigation menu contains all expected sections
    Then the navigation menu should contain the following items
      | Home      |
      | Products  |
      | Orders    |
      | Reports   |
      | Settings  |

  @TC-203
  Scenario: User can navigate to each section
    When the user navigates to "Products"
    Then the "Products" page should be loaded

  @TC-204
  Scenario: Notification bell shows correct count
    Then the notification bell count should be greater than or equal to 0

  @TC-205
  Scenario: Global search returns results
    When the user searches for "product"
    Then search results should be displayed
