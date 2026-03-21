package com.framework.pages;

import com.framework.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page Object for the Dashboard page.
 */
public class DashboardPage extends BasePage {

    // ── Locators ─────────────────────────────────────────────────────────
    private final By welcomeMessage   = By.cssSelector(".welcome-banner h2");
    private final By userAvatar       = By.cssSelector(".user-avatar");
    private final By logoutButton     = By.id("logout-btn");
    private final By navigationMenu   = By.cssSelector("nav.sidebar ul li");
    private final By notificationBell = By.id("notification-bell");
    private final By notificationCount= By.cssSelector(".notification-badge");
    private final By searchBar        = By.cssSelector("input.global-search");
    private final By profileDropdown  = By.id("profile-dropdown");
    private final By loadingSpinner   = By.cssSelector(".loading-spinner");

    // ── Actions ──────────────────────────────────────────────────────────

    @Step("Click logout")
    public LoginPage logout() {
        wait.waitForSpinnerToDisappear(loadingSpinner);
        click(userAvatar);
        click(logoutButton);
        return new LoginPage();
    }

    @Step("Navigate to section: {section}")
    public DashboardPage navigateTo(String section) {
        List<String> menuItems = getTextList(navigationMenu);
        menuItems.stream()
            .filter(item -> item.equalsIgnoreCase(section))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Menu item not found: " + section));

        click(By.xpath("//nav[contains(@class,'sidebar')]//li[normalize-space()='" + section + "']"));
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return this;
    }

    @Step("Search for: {query}")
    public DashboardPage search(String query) {
        type(searchBar, query);
        return this;
    }

    @Step("Open notifications")
    public DashboardPage openNotifications() {
        click(notificationBell);
        return this;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getWelcomeMessage() {
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return getText(welcomeMessage);
    }

    public int getNotificationCount() {
        String count = getText(notificationCount);
        return count.isBlank() ? 0 : Integer.parseInt(count.trim());
    }

    public List<String> getNavigationItems() {
        return getTextList(navigationMenu);
    }

    public boolean isUserLoggedIn() {
        return isDisplayed(userAvatar);
    }

    // ── Page Validation ──────────────────────────────────────────────────

    @Override
    public boolean isPageLoaded() {
        wait.waitForSpinnerToDisappear(loadingSpinner);
        return isDisplayed(welcomeMessage) && isDisplayed(userAvatar);
    }
}
