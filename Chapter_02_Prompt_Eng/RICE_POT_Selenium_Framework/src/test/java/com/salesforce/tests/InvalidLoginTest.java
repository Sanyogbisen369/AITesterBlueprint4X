package com.salesforce.tests;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.salesforce.pages.LoginPage;

public class InvalidLoginTest {

    private String baseUrl;
    private String username;
    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeTest
    public void loadConfiguration() {
        baseUrl = setting("sf.url", "SF_URL", "https://login.salesforce.com/?locale=in");
        username = setting("sf.username", "SF_USERNAME", "qa.automation@example.com");
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

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][] {
                {"wrong password", username, "Invalid@Pass#2026"},
                {"unregistered user", "no.such.user.qa@example.com", "Invalid@Pass#2026"},
                {"malformed username", "not-an-email", "Invalid@Pass#2026"}
        };
    }

    @Test(priority = 1, dataProvider = "invalidCredentials", description = "Invalid credentials are rejected with an error")
    public void verifyLoginFailsWithInvalidCredentials(String scenario, String user, String pass) {
        loginPage.doLogin(user, pass);
        assertLoginRejected(scenario);
    }

    @Test(priority = 2, description = "Submitting with empty username and password shows an error")
    public void verifyLoginFailsWithEmptyCredentials() {
        loginPage.clickLogin();
        assertLoginRejected("empty username and password");
    }

    @Test(priority = 3, description = "Submitting a username with an empty password shows an error")
    public void verifyLoginFailsWithEmptyPassword() {
        loginPage.enterUsername(username).proceedToPasswordStep();
        Assert.assertTrue(loginPage.isPasswordDisplayed(), "Password step should be shown before submitting empty password");
        loginPage.clickLogin();
        assertLoginRejected("empty password");
    }

    private void assertLoginRejected(String scenario) {
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Error should be displayed for " + scenario);
        Assert.assertFalse(loginPage.getErrorText().isBlank(), "Error text should not be blank for " + scenario);
        Assert.assertTrue(loginPage.getCurrentUrl().contains("login.salesforce.com"),
                "User should remain on login page for " + scenario + " but was on " + loginPage.getCurrentUrl());
    }

    private static String setting(String property, String env, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(env);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
