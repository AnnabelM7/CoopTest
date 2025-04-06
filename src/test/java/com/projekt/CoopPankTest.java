package com.projekt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoopPankTest {
    WebDriver driver;

    @BeforeEach
    void setup() {
        // Käivitab ChromeDriveri ja avab Coop Panga kodulaenu lehe
        //TODO: Lisa enda chromedriveri path
        System.setProperty("webdriver.chrome.driver", "C:\\path\\to\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);

        driver.get("https://www.cooppank.ee/eraklient/kodu/kodulaen");
    }

    /**
     * Kontrollib, et küpsiste nõusoleku bänner kaob pärast nupule "Nõustun kõigi küpsistega" vajutamist.
     */
    @Test
    void testClickCookiesButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement cookiesButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[text()='Nõustun kõigi küpsistega']/ancestor::button")));

        cookiesButton.click();

        boolean bannerIsGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//span[text()='Nõustun kõigi küpsistega']/ancestor::button")));

        Assertions.assertTrue(bannerIsGone, "Küpsiste banner peaks kaduma pärast nupule vajutamist");
    }
    /**
     * Kontrollib, et lehe pealkiri oleks "Kodulaenu kalkulaator".
     */
    @Test
    void testPageTitle() {
        testClickCookiesButton();
        String expectedText = "Kodulaenu kalkulaator";
        WebElement h3Element = driver.findElement(By.tagName("h3"));
        String actualText = h3Element.getText();
        assertEquals(expectedText, actualText, "h3 elemendi tekst ei vasta ootustele");
    }
    /**
     * Kontrollib, et liiga madal sissetulek kuvab veateate.
     */
    @Test
    void testIncomeValidation() {
        testClickCookiesButton();

        WebElement incomeField = driver.findElement(By.id("monthlyIncome"));
        incomeField.click();
        //incomeField.clear();
        incomeField.sendKeys(Keys.CONTROL + "a");
        incomeField.sendKeys(Keys.BACK_SPACE);

        incomeField.sendKeys("200");

        WebElement errorMessage = driver.findElement(By.cssSelector(".form-helper__text"));
        String expectedMessage = "Igakuine netosissetulek ei saa olla väiksem kui 700 EUR.";
        String actualMessage = errorMessage.getText();

        assertEquals(expectedMessage, actualMessage, "Sissetuleku valideerimise veateade ei ole õige");
    }
    /**
     * Kontrollib, et kui sisestatakse perioodiks 50 aastat, kuvatakse veateade
     * ja nupule "Esita taotlus" ei saa vajutada.
     */
    @Test
    void testLoanPeriodValidation() {
        testClickCookiesButton();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement periodField = driver.findElement(By.id("periodYears"));
        periodField.click();
        periodField.sendKeys(Keys.CONTROL + "a", Keys.BACK_SPACE);
        periodField.sendKeys("50");

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".form-helper__text")));
        String expectedError = "Periood saab olla 1-30 aastat.";
        String actualError = errorMessage.getText();
        assertEquals(expectedError, actualError, "Perioodi valideerimise veateade ei ole õige");

        WebElement submitButton = driver.findElement(By.xpath("//span[text()='Esita taotlus']/ancestor::button"));
        boolean isEnabled = submitButton.isEnabled();
        Assertions.assertFalse(isEnabled, "Nupp 'Esita taotlus' ei tohiks olla aktiivne vigase sisendi korral");
    }


    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
