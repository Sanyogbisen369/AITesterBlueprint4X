package com.salesforce.tests;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.salesforce.pages.LoginPage;

public class ValidLoginTest {

    private String baseUrl;
    private String username;
    private String password;
    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeTest
    public void loadConfiguration() {
        baseUrl = setting("sf.url", "SF_URL", "https://login.salesforce.com/?locale=in");
        username = setting("sf.username", "SF_USERNAME", "");
        password = setting("sf.password", "SF_PASSWORD", "");
    }

    @BeforeMethod
    public void setUp() {
        try {
            ChromeOptions options = new ChromeOptions();
            if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
                options.addArguments("--headless=new", "--window-size=1920,1080");
            }
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            loginPage = new LoginPage(driver).open(baseUrl);
        } catch (WebDriverException e) {
            tearDown();
            throw new IllegalStateException("Browser session could not be started", e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }

    @Test(priority = 1, description = "Login page renders username, login button and remember me controls")
    public void verifyLoginPageElementsAreDisplayed() {
        Assert.assertTrue(loginPage.isUsernameDisplayed(), "Username field should be visible");
        Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be visible");
        Assert.assertTrue(loginPage.isRememberMeDisplayed(), "Remember me checkbox should be visible");
        Assert.assertFalse(loginPage.getLoginButtonLabel().isBlank(), "Login button should have a label");
    }

    @Test(priority = 2, description = "Remember me checkbox can be checked and unchecked")
    public void verifyRememberMeToggle() {
        loginPage.setRememberMe(true);
        Assert.assertTrue(loginPage.isRememberMeSelected(), "Remember me should be selected");
        loginPage.setRememberMe(false);
        Assert.assertFalse(loginPage.isRememberMeSelected(), "Remember me should be cleared");
    }

    @Test(priority = 3, description = "Submitting a well-formed username advances to the password step")
    public void verifyUsernameStepRevealsPassword() {
        loginPage.enterUsername(username.isBlank() ? "qa.automation@example.com" : username)
                .proceedToPasswordStep();
        Assert.assertTrue(loginPage.isPasswordDisplayed(), "Password field should be visible after username step");
    }

    @Test(priority = 4, description = "Valid credentials log the user in")
    public void verifyLoginWithValidCredentials() {
        if (username.isBlank() || password.isBlank()) {
            throw new SkipException("Provide -Dsf.username and -Dsf.password (or SF_USERNAME / SF_PASSWORD) to run valid login");
        }
        loginPage.setRememberMe(true).doLogin(username, password);
        Assert.assertTrue(loginPage.waitForNavigationAwayFromLogin(),
                "Expected to leave the login page but stayed on " + loginPage.getCurrentUrl());
    }

    private static String setting(String property, String env, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(env);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
