package org.sparrow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestRgs {
    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void before() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        System.setProperty("webdriver.chrome.driver", "src/test/resources/webdriver/chromedriver_92.0.4515.107.exe");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        wait = new WebDriverWait(driver, 15, 1000);

        String baseUrl = "https://www.rgs.ru";
        driver.get(baseUrl);
    }

    @org.junit.Test
    public void test() {

        //закрыть куки
        String cookiesClose = "//*[@class='btn btn-default text-uppercase']";
        WebElement cookiesBtnClose = driver.findElement(By.xpath(cookiesClose));
        waitUtilElementToBeClickable(cookiesBtnClose);
        click(cookiesBtnClose);

        //выбрать меню
        String menuBtnXPath = "//div[@id='main-navbar-collapse']//a[contains(text(), 'Меню')]";
        WebElement menuBtn = driver.findElement(By.xpath(menuBtnXPath));
        click(menuBtn); //waitUtilElementToBeClickable нет, т.к. предполагаю pageLoadTimeout

        //выбрать "Компаниям"
        String companiesBtnXPath = "//div[@class='h3 adv-analytics-navigation-line2-link']/a[contains(text(), 'Компаниям')]";
        WebElement companiesBtn = driver.findElement(By.xpath(companiesBtnXPath));
        waitUtilElementToBeClickable(companiesBtn);
        click(companiesBtn);

        //выбрать "Страхование здоровья"
        String healthBtnXPath = "//a[contains(text(), 'Страхование здоровья')]";
        WebElement healthBtn = driver.findElement(By.xpath(healthBtnXPath));
        waitUtilElementToBeClickable(healthBtn);
        click(healthBtn);

        //переключиться на новую вкладку ДМС для сотрудников
        ArrayList<String> multipleTabs = new ArrayList<>(driver.getWindowHandles());
        driver.close();
        driver.switchTo().window(multipleTabs.get(1));

        Assert.assertEquals("Заголовок h1 отсутствует/не соответствует требуемому",
                "ДМС для сотрудников", //&nbsp не работает
                (driver.findElement(By.xpath("//h1"))).getAttribute("innerText"));

        //выбрать "Добровольное медицинское страхование"
        String dmsBtnXpath = "//div[@class='list-group list-group-rgs-menu collapse']/a[contains(text(), 'Добровольное медицинское страхование')]";
        WebElement dmsBtn = driver.findElement(By.xpath(dmsBtnXpath));
        waitUtilElementToBeClickable(dmsBtn);
        click(dmsBtn);

        Assert.assertEquals("Заголовок h1 отсутствует/не соответствует требуемому",
                "Добровольное медицинское страхование",
                (driver.findElement(By.xpath("//h1"))).getAttribute("innerText"));

        //нажать кнопку "отправить заявку"
        String checkoutBtnXpath = "//a[@class='btn btn-default text-uppercase hidden-xs adv-analytics-navigation-desktop-floating-menu-button']";
        WebElement checkoutBtn = driver.findElement(By.xpath(checkoutBtnXpath));
        waitUtilElementToBeClickable(checkoutBtn);
        click(checkoutBtn);

        //заполнить форму
        String parentFormXpath = "//*[@id='applicationForm']";
        WebElement parent = driver.findElement(By.xpath(parentFormXpath));

        WebElement lastName = parent.findElement(By.xpath(".//input[contains(@data-bind, 'LastName')]"));
        fillInputField(lastName, "Петров");

        WebElement firstName = parent.findElement(By.xpath(".//input[contains(@data-bind, 'FirstName')]"));
        fillInputField(firstName, "Петр");

        WebElement middleName = parent.findElement(By.xpath(".//input[contains(@data-bind, 'MiddleName')]"));
        fillInputField(middleName, "Петрович");

        WebElement email = parent.findElement(By.xpath(".//input[contains(@data-bind, 'Email')]"));
        fillInputField(email, "qwertyqwerty");

        WebElement comments = parent.findElement(By.xpath(".//textarea"));
        fillInputField(comments, "Комментарии");

        Select selectRegion = new Select(driver.findElement(By.xpath(".//select")));
        waitUtilElementToBeVisible(driver.findElement(By.xpath(".//select")));
        selectRegion.selectByVisibleText("Москва");//!!!нет проверки на перекрытие!!!

        WebElement checkbox = driver.findElement(By.xpath(".//input[@class='checkbox']"));//WebDriverWait.timeoutException если ставить любое ожидание перед кликом, хотя помех нет
        click(checkbox);

        WebElement phone = parent.findElement(By.xpath(".//input[contains(@data-bind, 'Phone')]"));
        click(phone);
        fillPhoneField(phone, "(111) 111-11-11", "+7 (111) 111-11-11");

        //Проверить, что все поля заполнены введенными значениями
        //проверка выполняется сразу после заполнения, для select не требуется
        Assert.assertTrue("Checkbox не выбран", checkbox.isSelected());

        //Нажать "отправить"
        click(driver.findElement(By.id("button-m")));

        //Проверить, что у поля "Эл. почта" присутствует сообщение об ошибке
        checkErrorMessageAtField(driver.findElement(By.xpath("//*[@id='applicationForm']//input[contains(@data-bind, 'Email')]")),
                "Введите адрес электронной почты");
    }

    @After
    public void after() {
        driver.quit();
    }

    //Явное ожидание того что элемент станет видимым
    private void waitUtilElementToBeVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    //Явное ожидание того что элемент станет кликабельным
    private void waitUtilElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    private void fillInputField(WebElement element, String value) {
        waitUtilElementToBeClickable(element);
        click(element);
        element.clear();
        element.sendKeys(value);
        Assert.assertEquals("Поле было заполнено некорректно", value, element.getAttribute("value"));
    }

    private void fillPhoneField(WebElement element, String inputValue, String resultValue) {
        waitUtilElementToBeClickable(element);
        click(element);
        element.clear();
        element.sendKeys(inputValue);
        try {
            Assert.assertEquals("Поле было заполнено некорректно", resultValue, element.getAttribute("value"));
        } catch (ComparisonFailure ignore) {
            fillPhoneField(element, inputValue, resultValue);
        }
    }

    private void checkErrorMessageAtField(WebElement element, String errorMessage) {
        element = element.findElement(By.xpath("./../div//span"));
        Assert.assertEquals("Проверка ошибки у поля не была пройдена",
                errorMessage, element.getAttribute("innerText"));
    }

    private void click(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException ignore) {
            closeDynamicFrame("fl-498072", "//div[@class='Ribbon-close']");
            closeDynamicFrame("fl-501173", "//div[@class='widget__close js-collapse-login']");
            click(element);
        }
    }

    private void closeDynamicFrame(String frameId, String frameXpath) {
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        try {
            driver.switchTo().frame(frameId);
            WebElement frameBtnClose = driver.findElement(By.xpath(frameXpath));
            frameBtnClose.click();
            driver.switchTo().defaultContent();
        } catch (NoSuchFrameException ignore) {
        } finally {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
    }
}
