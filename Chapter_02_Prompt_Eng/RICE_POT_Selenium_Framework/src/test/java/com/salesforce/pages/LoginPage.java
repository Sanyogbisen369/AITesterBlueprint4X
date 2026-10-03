package com.salesforce.pages;

import java.time.Duration;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//input[@id='username']")
    private WebElement usernameInput;

    @FindBy(xpath = "//input[@id='password']")
    private WebElement passwordInput;

    @FindBy(xpath = "//input[@id='Login']")
    private WebElement loginButton;

    @FindBy(xpath = "//input[@id='rememberUn']")
    private WebElement rememberMeCheckbox;

    @FindBy(xpath = "//div[@id='error']")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
        PageFactory.initElements(driver, this);
    }

    public LoginPage open(String url) {
        try {
            driver.get(url);
            wait.until(ExpectedConditions.visibilityOf(loginButton));
            return this;
        } catch (TimeoutException e) {
            throw new IllegalStateException("Login page did not load: " + url, e);
        }
    }

    public LoginPage enterUsername(String username) {
        try {
            WebElement field = wait.until(ExpectedConditions.visibilityOf(usernameInput));
            field.clear();
            field.sendKeys(username);
            return this;
        } catch (TimeoutException | NoSuchElementException e) {
            throw new IllegalStateException("Username field is not available", e);
        }
    }

    public LoginPage enterPassword(String password) {
        try {
            WebElement field = wait.until(ExpectedConditions.visibilityOf(passwordInput));
            field.clear();
            field.sendKeys(password);
            return this;
        } catch (TimeoutException | NoSuchElementException e) {
            throw new IllegalStateException("Password field is not available", e);
        }
    }

    public LoginPage clickLogin() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
            return this;
        } catch (TimeoutException | StaleElementReferenceException e) {
            throw new IllegalStateException("Login button is not clickable", e);
        }
    }

    public LoginPage proceedToPasswordStep() {
        if (isVisible(passwordInput)) {
            return this;
        }
        clickLogin();
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOf(passwordInput),
                    ExpectedConditions.visibilityOf(errorMessage)));
            return this;
        } catch (TimeoutException e) {
            throw new IllegalStateException("Neither password step nor error appeared after submitting username", e);
        }
    }

    public LoginPage doLogin(String username, String password) {
        enterUsername(username);
        proceedToPasswordStep();
        if (isVisible(passwordInput)) {
            enterPassword(password);
            clickLogin();
        }
        return this;
    }

    public LoginPage setRememberMe(boolean checked) {
        try {
            WebElement checkbox = wait.until(ExpectedConditions.elementToBeClickable(rememberMeCheckbox));
            if (checkbox.isSelected() != checked) {
                checkbox.click();
            }
            return this;
        } catch (TimeoutException e) {
            throw new IllegalStateException("Remember me checkbox is not clickable", e);
        }
    }

    public boolean isRememberMeSelected() {
        try {
            return rememberMeCheckbox.isSelected();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getErrorText() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText().trim();
        } catch (TimeoutException e) {
            throw new IllegalStateException("Error message did not appear", e);
        }
    }

    public boolean isUsernameDisplayed() {
        return isVisible(usernameInput);
    }

    public boolean isPasswordDisplayed() {
        return isVisible(passwordInput);
    }

    public boolean isLoginButtonDisplayed() {
        return isVisible(loginButton);
    }

    public boolean isRememberMeDisplayed() {
        return isVisible(rememberMeCheckbox);
    }

    public String getLoginButtonLabel() {
        return loginButton.getAttribute("value");
    }

    public boolean waitForNavigationAwayFromLogin() {
        try {
            return wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.salesforce.com")));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    private boolean isVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
