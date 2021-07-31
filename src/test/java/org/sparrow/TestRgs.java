package org.sparrow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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
        System.setProperty("webdriver.chrome.driver", "src/test/resources/webdriver/chromedriver_92.0.4515.107.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        wait = new WebDriverWait(driver, 20, 1000);

        String baseUrl = "https://www.rgs.ru";
        driver.get(baseUrl);
    }

    @org.junit.Test
    public void test() {

        //закрыть куки
        String cookiesClose = "//*[@class='btn btn-default text-uppercase']";
        WebElement cookiesBtnClose = driver.findElement(By.xpath(cookiesClose));
        cookiesBtnClose.click();

        //выбрать меню
        String menuBtnXPath = "//div[@id='main-navbar-collapse']//a[contains(text(), 'Меню')]";
        WebElement menuBtn = driver.findElement(By.xpath(menuBtnXPath));
        menuBtn.click();

        //выбрать "Компаниям"
        String companiesBtnXPath = "//div[@class='h3 adv-analytics-navigation-line2-link']/a[contains(text(), 'Компаниям')]";
        WebElement companiesBtn = driver.findElement(By.xpath(companiesBtnXPath));
        waitUtilElementToBeClickable(companiesBtn);
        companiesBtn.click();

        //выбрать "Страхование здоровья"
        String healthBtnXPath = "//a[contains(text(), 'Страхование здоровья')]";
        WebElement healthBtn = driver.findElement(By.xpath(healthBtnXPath));
        waitUtilElementToBeClickable(healthBtn);
        healthBtn.click();

        //переключиться на новую вкладку ДМС для сотрудников
        ArrayList<String> multipleTabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(multipleTabs.get(1));

        Assert.assertEquals("Заголовок h1 отсутствует/не соответствует требуемому",
                "ДМС для сотрудников", //&nbsp не работает
                (driver.findElement(By.xpath("//h1"))).getAttribute("innerText"));

        //выбрать "Добровольное медицинское страхование"
        String dmsBtnXpath = "//div[@class='list-group list-group-rgs-menu collapse']/a[contains(text(), 'Добровольное медицинское страхование')]";
        WebElement dmsBtn = driver.findElement(By.xpath(dmsBtnXpath));
        waitUtilElementToBeClickable(dmsBtn);
        driver.findElement(By.xpath(dmsBtnXpath)).click();

        Assert.assertEquals("Заголовок h1 отсутствует/не соответствует требуемому",
                "Добровольное медицинское страхование",
                (driver.findElement(By.xpath("//h1"))).getAttribute("innerText"));

        //нажать кнопку "отправить заявку"
        String checkoutBtnXpath = "//a[@class='btn btn-default text-uppercase hidden-xs adv-analytics-navigation-desktop-floating-menu-button']";
        WebElement checkoutBtn = driver.findElement(By.xpath(checkoutBtnXpath));
        waitUtilElementToBeClickable(checkoutBtn);
        checkoutBtn.click();

        // заполнить форму
        String parentFormXpath = "//*[@id='applicationForm']";
        WebElement parent = driver.findElement(By.xpath(parentFormXpath));

        Select selectRegion = new Select(driver.findElement(By.xpath(".//select")));
        waitUtilElementToBeVisible(driver.findElement(By.xpath(".//select")));
        selectRegion.selectByVisibleText("Москва");

        WebElement checkbox = driver.findElement(By.xpath(".//input[@class='checkbox']"));
        checkbox.click();

        WebElement phone = parent.findElement(By.xpath(".//input[contains(@data-bind, 'Phone')]"));
        waitUtilElementToBeVisible(phone);
        phone.click();
        fillInputField(phone, "--------------------------111111-11-11");

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

        //Проверить, что все поля заполнены введенными значениями
        Assert.assertTrue("Checkbox не выбран", checkbox.isSelected());
        checkValue(lastName, "Петров");
        checkValue(firstName, "Петр");
        checkValue(middleName, "Петрович");
        checkValue(email, "qwertyqwerty");
        checkValue(comments, "Комментарии");
        checkValue(phone, "+7 (111) 111-11-11");
        WebElement region = driver.findElement(By.xpath(".//select"));
        Assert.assertEquals("Поле было заполнено некорректно", "77", region.getAttribute("value"));

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
        element.click();
        element.clear();
        element.sendKeys(value);
    }

    private void checkValue(WebElement element, String value) {
        Assert.assertEquals("Поле было заполнено некорректно", value, element.getAttribute("value"));
    }

    private void checkErrorMessageAtField(WebElement element, String errorMessage) {
        element = element.findElement(By.xpath("./../div//span"));
        Assert.assertEquals("Проверка ошибки у поля не была пройдена",
                errorMessage, element.getAttribute("innerText"));
    }
}
