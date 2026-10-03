Written for: your course notes. It records the plan as the framework was actually built, including the corrections the live page forced. Replace what's in [04_Plan_Framework.md](Chapter_02_Prompt_Eng/04_Plan_Framework.md) with this:

~~~markdown
# Plan: Salesforce Login Automation Framework (RICE POT)

## 1. Context
The RICE POT prompt in `02_RICE_POT.example.md` asks for an enterprise Selenium + Java + Maven + TestNG framework.
It must test the Salesforce login page (`https://login.salesforce.com/?locale=in`) with valid and invalid cases.

## 2. Decoding the RICE POT Prompt
| Layer | What it demands |
|---|---|
| **R**ole | QA automation architect, 15 yrs, CRM/Salesforce domain |
| **I**nstructions | Selenium + Java + Maven + TestNG; valid + invalid login tests; TestNG annotations; try/catch exception handling; POM with PageFactory + `@FindBy`; xpath only |
| **C**ontext | Salesforce login page with username, password, Log In button, Remember me |
| **E**xample | `@FindBy(xpath=...)`, `PageFactory.initElements(driver, this)`, `doLogin()` action method |
| **P**arameters | Production-grade; external URLs and credentials supplied separately |
| **O**utput | Only 1 Page Object, 2 TestNG scripts, Maven project |
| **T**one | Technical, precise; no comments, no `Thread.sleep`, no CSS/ID/name locators |

**Scope decision:** strict output. No BaseTest, ConfigReader, or WaitUtils classes, because the prompt caps the deliverable at 3 code files plus Maven files.

## 3. Live DOM Findings (verified in a real Chrome browser)
- The login is **identifier-first**: only Username, Log In, and Remember me show at first.
- Submitting a username reveals the password field.
- Errors render in `div#error` (class `loginError`), for example "Please enter your username." or "Please check your username and password."
- After any submit, the URL changes to `https://login.salesforce.com/` (the `?locale=in` is dropped).

| Element | XPath |
|---|---|
| Username | `//input[@id='username']` |
| Password (appears after the username step) | `//input[@id='password']` |
| Log In button | `//input[@id='Login']` |
| Remember me | `//input[@id='rememberUn']` |
| Error message | `//div[@id='error']` |

## 4. Project Structure
```
RICE_POT_Selenium_Framework/
├── pom.xml
├── testng.xml
└── src/test/java/com/salesforce/
    ├── pages/LoginPage.java
    └── tests/
        ├── ValidLoginTest.java
        └── InvalidLoginTest.java
```

## 5. Implementation Steps
1. **pom.xml**: Java 17 release, `selenium-java` 4.50.0, `testng` 7.12.0, Surefire pointing at `testng.xml`.
   - No WebDriverManager is needed; Selenium Manager resolves chromedriver.
   - Selenium 4.25 failed with Chrome 154, so use a recent Selenium version.
2. **testng.xml**: one suite with `ValidLoginTest` and `InvalidLoginTest`.
3. **LoginPage.java**:
   - The 5 xpath `@FindBy` fields from section 3, plus a `WebDriverWait` (15s).
   - Actions: `open`, `enterUsername`, `enterPassword`, `clickLogin`, `proceedToPasswordStep`, `doLogin`, `setRememberMe`.
   - Checks: `isErrorDisplayed`, `getErrorText`, visibility getters, `waitForNavigationAwayFromLogin`.
   - `doLogin` handles the two steps: enter the username, submit, wait for the password field or an error, then enter the password and submit.
   - Each action catches `TimeoutException` / `NoSuchElementException` and rethrows it with a clear message.
4. **Test classes** (each has its own lifecycle, since there is no BaseTest):
   - `@BeforeTest` reads config from `-D` system properties or env vars, with no hardcoded credentials:

     | Setting | System property | Env var |
     |---|---|---|
     | URL | `sf.url` | `SF_URL` |
     | Username | `sf.username` | `SF_USERNAME` |
     | Password | `sf.password` | `SF_PASSWORD` |

   - `@BeforeMethod` starts a fresh ChromeDriver for every test (headless when `-Dheadless=true`) and opens the login page.
   - `@AfterMethod(alwaysRun = true)` quits the driver safely.

## 6. Test Matrix
| # | Class | Test | Expected |
|---|---|---|---|
| 1 | Valid | Login page elements displayed | Username, Log In, Remember me visible |
| 2 | Valid | Remember me toggle | Checkbox checks and unchecks |
| 3 | Valid | Username step reveals password | Password field visible |
| 4 | Valid | Login with valid credentials | Leaves `login.salesforce.com` (skipped if no credentials) |
| 5 | Invalid | Wrong password | Error shown, stays on login page |
| 6 | Invalid | Unregistered user | Error shown, stays on login page |
| 7 | Invalid | Malformed username | Error shown, stays on login page |
| 8 | Invalid | Empty username and password | "Please enter your username" error |
| 9 | Invalid | Empty password | Error shown after the password step |

- Cases 5–7 run through one `@DataProvider`.
- Invalid tests only check that the error is shown and not blank, not its exact text, because the text varies by locale.

## 7. Constraint Checklist
- [x] XPath-only `@FindBy`; no `By.id`, `By.name`, `cssSelector`, or `className`
- [x] No `Thread.sleep`; only `WebDriverWait` + `ExpectedConditions`
- [x] No comments in the Java files
- [x] PageFactory, constructor initialization, reusable action methods
- [x] TestNG annotations: `@BeforeTest`, `@BeforeMethod`, `@AfterMethod`, `@Test`, `@DataProvider`
- [x] Exception handling in both the page and the tests

## 8. Verification
```bash
mvn test -Dheadless=true
mvn test -Dsf.username=you@org.com -Dsf.password=****
grep -rnE "Thread\.sleep|By\.(id|name|cssSelector|className)|^\s*//|/\*" src
```
- The first command runs without credentials; the result was 9 run, 0 failed, 1 skipped.
- The second command adds a real login (optionally with `-Dsf.url=<staging-url>`).
- The grep should find nothing.

## 9. Risks
- Salesforce may require identity verification or CAPTCHA on the first login from a new device, which blocks the valid login test.
- Salesforce A/B tests its login page, so the DOM may change. Re-check the xpaths if the tests start failing.
- Add `target/` to `.gitignore` before committing.
~~~