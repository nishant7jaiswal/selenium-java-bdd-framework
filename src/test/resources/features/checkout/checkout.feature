# ──────────────────────────────────────────────────────────────────────────────
# Feature: Checkout
# Module:  E-Commerce Checkout Flow
# Author:  Nishant Jaiswal
# ──────────────────────────────────────────────────────────────────────────────

@checkout @regression
Feature: Checkout Flow

  Background:
    Given the user is logged in as "standard_user"
    And the user has items in the cart

  @smoke @TC-301
  Scenario: User can view cart items before checkout
    Then the cart should display at least 1 item
    And the cart total should be greater than zero

  @TC-302
  Scenario: User can remove an item from cart
    Given the cart has more than 1 item
    When the user removes the first item from the cart
    Then the cart item count should decrease by 1

  @smoke @TC-303
  Scenario: User can complete end-to-end checkout
    When the user proceeds to checkout
    And the user fills in shipping details
      | firstName | lastName | address          | city  | state      | zip   | country |
      | Nishant   | Jaiswal  | 123 Test Street  | Pune  | Maharashtra| 411001| India   |
    And the user fills in payment details
      | cardNumber       | expiry | cvv |
      | 4111111111111111 | 12/26  | 123 |
    And the user places the order
    Then the order confirmation should be displayed
    And an order number should be generated

  @TC-304 @JIRA-CART-512
  Scenario Outline: Checkout with different shipping locations
    When the user proceeds to checkout
    And the user fills in shipping details for "<city>" in "<country>"
    And the user fills in payment details
      | cardNumber       | expiry | cvv |
      | 4111111111111111 | 12/26  | 123 |
    And the user places the order
    Then the order confirmation should be displayed

    Examples:
      | city      | country       |
      | Mumbai    | India         |
      | Bangalore | India         |
      | Delhi     | India         |

  @TC-305
  Scenario: Cart persists after page refresh
    When the user refreshes the page
    Then the cart should still contain the same items

  @wip @TC-306
  Scenario: Apply discount coupon at checkout
    When the user proceeds to checkout
    And the user applies coupon code "SAVE10"
    Then the cart total should be reduced by 10%
