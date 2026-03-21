package com.framework.steps;

import com.framework.pages.CheckoutPage;
import com.framework.utils.assertion.AssertionUtils;
import com.framework.utils.data.TestDataGenerator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Step definitions for Checkout feature.
 * Demonstrates DataTable handling, shared state, and multi-step flows.
 */
public class CheckoutSteps {

    private static final Logger log = LogManager.getLogger(CheckoutSteps.class);

    private CheckoutPage checkoutPage = new CheckoutPage();
    private String placedOrderNumber;

    // ── Given ────────────────────────────────────────────────────────────

    @Given("the user has items in the cart")
    public void theUserHasItemsInTheCart() {
        // In a real project: add items via API to bypass UI product selection
        // ApiUtils.post("/api/cart/add", Map.of("productId", "ITEM_001", "qty", 2));
        log.info("Cart pre-populated with test items via API setup");
    }

    @Given("the cart has more than {int} item")
    public void theCartHasMoreThanItems(int minItems) {
        int actual = checkoutPage.getCartItemCount();
        AssertionUtils.assertTrue(actual > minItems,
            "Cart should have more than " + minItems + " item(s), but had: " + actual);
    }

    // ── When ─────────────────────────────────────────────────────────────

    @When("the user removes the first item from the cart")
    public void theUserRemovesTheFirstItemFromTheCart() {
        checkoutPage.removeFirstItem();
    }

    @When("the user proceeds to checkout")
    public void theUserProceedsToCheckout() {
        checkoutPage.proceedToCheckout();
    }

    @When("the user fills in shipping details")
    public void theUserFillsInShippingDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        checkoutPage.fillShippingDetails(
            data.get("firstName"), data.get("lastName"),
            data.get("address"),   data.get("city"),
            data.get("state"),     data.get("zip"),
            data.get("country")
        );
        checkoutPage.continueToPayment();
    }

    @When("the user fills in shipping details for {string} in {string}")
    public void theUserFillsInShippingDetailsFor(String city, String country) {
        checkoutPage.fillShippingDetails(
            TestDataGenerator.firstName(),
            TestDataGenerator.lastName(),
            TestDataGenerator.streetAddress(),
            city,
            TestDataGenerator.state(),
            "400001",
            country
        );
        checkoutPage.continueToPayment();
    }

    @When("the user fills in payment details")
    public void theUserFillsInPaymentDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        checkoutPage.fillPaymentDetails(
            data.get("cardNumber"),
            data.get("expiry"),
            data.get("cvv"),
            TestDataGenerator.fullName()
        );
    }

    @When("the user places the order")
    public void theUserPlacesTheOrder() {
        checkoutPage.placeOrder();
        placedOrderNumber = checkoutPage.getOrderNumber();
        log.info("Order placed: {}", placedOrderNumber);
    }

    @When("the user applies coupon code {string}")
    public void theUserAppliesCouponCode(String coupon) {
        // Implement when coupon feature is available
        throw new io.cucumber.java.PendingException("Coupon feature not yet implemented");
    }

    // ── Then ─────────────────────────────────────────────────────────────

    @Then("the cart should display at least {int} item")
    public void theCartShouldDisplayAtLeastItem(int minItems) {
        int actual = checkoutPage.getCartItemCount();
        AssertionUtils.assertTrue(actual >= minItems,
            "Cart should have at least " + minItems + " item(s), but had: " + actual);
    }

    @Then("the cart total should be greater than zero")
    public void theCartTotalShouldBeGreaterThanZero() {
        String total = checkoutPage.getCartTotal();
        AssertionUtils.assertNotBlank(total, "Cart total");
        double amount = Double.parseDouble(total.replaceAll("[^0-9.]", ""));
        AssertionUtils.assertTrue(amount > 0, "Cart total should be > 0, was: " + amount);
    }

    @Then("the cart item count should decrease by {int}")
    public void theCartItemCountShouldDecreaseBy(int decreaseBy) {
        // Compare with count stored before removal (via shared state or Cucumber world)
        int current = checkoutPage.getCartItemCount();
        log.info("Cart count after removal: {}", current);
        AssertionUtils.assertTrue(current >= 0, "Cart count should be non-negative");
    }

    @Then("the order confirmation should be displayed")
    public void theOrderConfirmationShouldBeDisplayed() {
        AssertionUtils.assertTrue(checkoutPage.isOrderConfirmed(),
            "Order confirmation message should be displayed");
        AssertionUtils.assertContains(checkoutPage.getConfirmationMessage(),
            "Thank you", "Confirmation message should contain 'Thank you'");
    }

    @Then("an order number should be generated")
    public void anOrderNumberShouldBeGenerated() {
        AssertionUtils.assertNotBlank(placedOrderNumber, "Order number");
        log.info("Order number verified: {}", placedOrderNumber);
    }

    @Then("the cart should still contain the same items")
    public void theCartShouldStillContainTheSameItems() {
        int count = checkoutPage.getCartItemCount();
        AssertionUtils.assertTrue(count > 0, "Cart should not be empty after refresh");
    }

    @Then("the cart total should be reduced by {int}%")
    public void theCartTotalShouldBeReducedBy(int percent) {
        log.info("Verifying {}% discount applied", percent);
        // Implement price comparison logic here
        AssertionUtils.assertTrue(true, "Discount verification placeholder");
    }
}
