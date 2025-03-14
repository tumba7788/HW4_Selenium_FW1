package ui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.List;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HomePageTests {
    private static final Logger logger = LoggerFactory.getLogger(HomePageTests.class); // Нужно для логирования, для вывода List на печать
    WebDriver driver;
    private static final String BASE_URL = "https://bonigarcia.dev/selenium-webdriver-java/";

    @BeforeEach
    void setUp(){
        driver = new ChromeDriver();
        driver.get(BASE_URL); //передать адрес домашней страницы
        driver.manage().window().maximize(); //открыть браузер во весь экран
    }

    @AfterEach
    void tearDown(){
        driver.quit(); // закрыть весь браузер
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 1)
    void checkChapterTitlesTest(String chapterName, String linkText){

        // 1 проверяем название раздела
        WebElement chapterTitle = driver.findElement(By.xpath("//div/h5[text() = '"+ chapterName +"']")); //div/h5[text() = 'Chapter 3. WebDriver Fundamentals']

        Assertions.assertEquals(chapterName, chapterTitle.getText(), "Chapter name должен быть" + chapterName);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 1)
    void checkHomePageLinksTest(String chapterName, String linkUrl, String homePageButtonName)  {
        // 2 Находим кнопку раздела и переходим по нему
        WebElement buttonToClick = driver.findElement(By.xpath("//h5[contains(@class,'card-title')]/../a[text() = '" + homePageButtonName + "']"));

        buttonToClick.click(); // Кликаем по найденной кнопке
        String actualUrl = driver.getCurrentUrl(); // Получить адрес текущей страницы
        Assertions.assertEquals(linkUrl, actualUrl, "Адрес страницы должен быть " + linkUrl);

    }
    @ParameterizedTest
   // Disabled ("этот тест следует выполнять один раз и не в параллели!, с целью автоматического получения titles.txt, где сохранятся все заголовки открывающихся страниц")
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 1)
    void getPageTitlesTest(String chapterName, String linkUrl, String homePageButtonName, String titlesLinkedHomePages, boolean isFrame) {


        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> buttons = driver.findElements(By.xpath("//a[text() ='" + homePageButtonName + "']"));

        try (FileWriter writer = new FileWriter("titles.txt", true)) {  // Appending to the file
            for (int i = 0; i < buttons.size(); i++) {
                // Refresh buttons list after each navigation
                buttons = driver.findElements(By.xpath("//a[text() ='" + homePageButtonName + "']"));

               buttons.get(i).click();

                // Switch to iframe
                if (isFrame) {
                    System.out.println("SWITCH TO FRAME");
                    WebElement frameElement = driver.findElement(By.xpath("//frame[@name = 'frame-header']"));
                    driver.switchTo().frame(frameElement);
                }

                // Wait until the page is loaded and the title element is present
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".display-6")));
                } catch (TimeoutException e) {
                    System.out.println("Element .display-6 not found on the page.");
                    continue; // Skip this iteration if element is not found
                }



                // Get the title
                String title = null;
                try {
                    title = driver.findElement(By.xpath("//h1[contains(@class,'display-6')]")).getText();
                } catch (NoSuchElementException e) {
                    System.out.println("Title element not found on the page.");
                    continue;
                }

                // Output and write the title to a file
                System.out.println("Page title: " + title);
                writer.write(title + "\n");

                // Return to the starting page
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[text() ='" + homePageButtonName + "']")));

                // Switch back to the main content if inside iframe
                if (isFrame) {
                    driver.switchTo().defaultContent();
                }
            }
        } catch (IOException e) {
            System.err.println("Error during writing file: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 1)
    void checkTitleLinkedWithHomePageTest(String chapterName, String linkUrl, String homePageButtonName, String titlesLinkedHomePages, boolean isFrame) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement buttonToClick = driver.findElement(By.xpath("//h5[contains(@class,'card-title')]/../a[text() = '" + homePageButtonName + "']"));
        buttonToClick.click(); // Кликаем по найденной кнопке

        // Switch to iframe
        if (isFrame) {
            WebElement frameElement = driver.findElement(By.xpath("//frame[@name = 'frame-header']"));
            driver.switchTo().frame(frameElement);
        }

        // Wait until the page is loaded and the title element is present
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".display-6")));
        } catch (TimeoutException e) {
            System.out.println("Element .display-6 not found on the page.");
        }


        String actualTitle = null;
        try {
            actualTitle = driver.findElement(By.xpath("//h1[contains(@class,'display-6')]")).getText();
        } catch (NoSuchElementException e) {
            System.out.println("Title element not found on the page.");
        }

        // Switch back to the main content if inside iframe
        if (isFrame) {
            driver.switchTo().defaultContent();
        }

        Assertions.assertEquals(titlesLinkedHomePages,actualTitle,"Название страницы не совпадает");
    }
    }











//    @Test
//    void openHomePageTest(){
//        String actualTitle = driver.getTitle();
//        Assertions.assertEquals("Hands-On Selenium WebDriver with Java", actualTitle );
//    }
//
//    @Test
//    void openChapter3WebForm(){
//
//        // Способ 1: Найти с помощью xPath элемент и кликнуть его
//
//      WebElement webFormLink = driver.findElement(By.xpath("//a[@href = 'web-form.html']"));
//        webFormLink.click();
//
//        // Способ 2: Найти с помощью текста элемент и кликнуть его, без сохранения в переменную
//       // driver.findElement(By.linkText("Web form")).click();
//
//        //Или вариант2:
//
//        driver.findElement(By.xpath("//h5[text() = 'Chapter 3. WebDriver Fundamentals']/../a[contains(@href, 'web-form.html')]")).click();
//
//
//
//        String webFormURL = "web-form.html";
//        String currentURL = driver.getCurrentUrl(); // https://bonigarcia.dev/selenium-webdriver-java/web-form.html
//
//        WebElement title = driver.findElement(By.className("display-6"));
//
//        Assertions.assertEquals(BASE_URL + webFormURL,currentURL);
//        Assertions.assertEquals("Web form", title.getText());
//    }
//
//    @Test
//    void openChapter3Navigation() throws InterruptedException {
//       // driver.findElement(By.className("btn btn-outline-primary mb-2"));
//        driver.findElement(By.xpath("//h5[text()='Chapter 3. WebDriver Fundamentals']/../a[contains(@href,'navigation1')]")).click();
//        String navigationPageURL= driver.getCurrentUrl();
//        String navigationElement = driver.findElement(By.className("display-6")).getText();
//
//        Thread.sleep(2000);
//
//        Assertions.assertEquals(BASE_URL + "navigation1.html", navigationPageURL );
//        Assertions.assertEquals("Navigation example", navigationElement);
//    }



