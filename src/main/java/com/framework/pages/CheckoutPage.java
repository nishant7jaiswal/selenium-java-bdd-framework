package com.framework.pages;

import com.framework.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page Object for Checkout flow.
 * Demonstrates multi-step form handling, table parsing,
 * and dynamic element interactions.
 */
public class CheckoutPage extends BasePage {

    // ── Locators — Cart ──────────────────────────────────────────────────
    private final By cartItemRows     = By.cssSelector(".cart-item");
    private final By cartItemName     = By.cssSelector(".cart-item .item-name");
    private final By cartItemPrice    = By.cssSelector(".cart-item .item-price");
    private final By cartItemQty      = By.cssSelector(".cart-item .item-qty");
    private final By removeItemBtn    = By.cssSelector(".cart-item .remove-btn");
    private final By cartTotal        = By.cssSelector(".cart-summary .total-amount");
    private final By proceedBtn       = By.id("proceed-to-checkout");
    private final By continueShopBtn  = By.id("continue-shopping");

    // ── Locators — Shipping ──────────────────────────────────────────────
    private final By firstNameField   = By.id("shipping-first-name");
    private final By lastNameField    = By.id("shipping-last-name");
    private final By addressField     = By.id("shipping-address");
    private final By cityField        = By.id("shipping-city");
    private final By stateDropdown    = By.id("shipping-state");
    private final By zipField         = By.id("shipping-zip");
    private final By countryDropdown  = By.id("shipping-country");
    private final By continueBtn      = By.id("continue-to-payment");

    // ── Locators — Payment ───────────────────────────────────────────────
    private final By cardNumberField  = By.id("card-number");
    private final By expiryField      = By.id("card-expiry");
    private final By cvvField         = By.id("card-cvv");
    private final By cardNameField    = By.id("card-name");
    private final By placeOrderBtn    = By.id("place-order");

    // ── Locators — Confirmation ──────────────────────────────────────────
    private final By confirmationMsg  = By.cssSelector(".order-confirmation h2");
    private final By orderNumber      = By.cssSelector(".order-number");
    private final By loadingSpinner   = By.cssSelector(".checkout-spinner");

    // ── Cart Actions ─────────────────────────────────────────────────────

    @Step("Get cart item count")
    public int getCartItemCount() {
        return getElementCount(cartItemRows);
    }

    @Step("Get cart total")
    public String getCartTotal() {
        return getText(cartTotal);
    }

    @Step("Get all cart item names")
    public List<String> getCartItemNames() {
        return getTextList(cartItemName);
    }

    @Step("Remove first item from cart")
    public CheckoutPage removeFirstItem() {
        click(removeItemBtn);
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return this;
    }

    @Step("Click Proceed to Checkout")
    public CheckoutPage proceedToCheckout() {
        click(proceedBtn);
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return this;
    }

    // ── Shipping Form ────────────────────────────────────────────────────

    @Step("Fill shipping address")
    public CheckoutPage fillShippingDetails(String firstName, String lastName,
                                            String address, String city,
                                            String state, String zip, String country) {
        type(firstNameField, firstName);
        type(lastNameField, lastName);
        type(addressField, address);
        type(cityField, city);
        selectByVisibleText(stateDropdown, state);
        type(zipField, zip);
        selectByVisibleText(countryDropdown, country);
        return this;
    }

    @Step("Continue to payment")
    public CheckoutPage continueToPayment() {
        click(continueBtn);
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return this;
    }

    // ── Payment Form ─────────────────────────────────────────────────────

    @Step("Fill payment details")
    public CheckoutPage fillPaymentDetails(String cardNumber, String expiry,
                                           String cvv, String nameOnCard) {
        // Credit card fields often inside iframes — switch if needed
        type(cardNumberField, cardNumber);
        type(expiryField, expiry);
        type(cvvField, cvv);
        type(cardNameField, nameOnCard);
        return this;
    }

    @Step("Place order")
    public CheckoutPage placeOrder() {
        click(placeOrderBtn);
        wait.waitForSpinnerToDisappear(loadingSpinner);
        wait.waitForElementVisible(confirmationMsg, 30);
        return this;
    }

    // ── Confirmation ─────────────────────────────────────────────────────

    public String getConfirmationMessage() {
        return getText(confirmationMsg);
    }

    public String getOrderNumber() {
        return getText(orderNumber).replaceAll("[^0-9]", "");
    }

    public boolean isOrderConfirmed() {
        return isDisplayed(confirmationMsg) &&
               getText(confirmationMsg).toLowerCase().contains("thank you");
    }

    // ── Fluent End-to-End Helper ──────────────────────────────────────────

    /**
     * Completes the full checkout flow in one call.
     * Useful for tests where checkout is a prerequisite, not the SUT.
     */
    @Step("Complete full checkout flow")
    public String completeCheckout(String firstName, String lastName,
                                   String address, String city, String state,
                                   String zip, String country,
                                   String cardNumber, String expiry, String cvv) {
        proceedToCheckout()
            .fillShippingDetails(firstName, lastName, address, city, state, zip, country)
            .continueToPayment()
            .fillPaymentDetails(cardNumber, expiry, cvv, firstName + " " + lastName)
            .placeOrder();

        return getOrderNumber();
    }

    // ── Page Validation ──────────────────────────────────────────────────

    @Override
    public boolean isPageLoaded() {
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return isDisplayed(cartTotal) || isDisplayed(confirmationMsg);
    }
}
