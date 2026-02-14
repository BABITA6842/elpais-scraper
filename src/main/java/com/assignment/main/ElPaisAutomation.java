package com.assignment.main;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class ElPaisAutomation {

    public static void handleCookies(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement acceptBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(.,'Aceptar') or contains(.,'Accept')]")));
            acceptBtn.click();
        } catch (Exception ignored) {}
    }

    public static String translateToEnglish(String text) {
        try {
            String urlStr = "https://api.mymemory.translated.net/get?q="
                    + java.net.URLEncoder.encode(text, "UTF-8")
                    + "&langpair=es|en";

            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String json = response.toString();
            int start = json.indexOf("\"translatedText\":\"") + 18;
            int end = json.indexOf("\"", start);

            return json.substring(start, end);

        } catch (Exception e) {
            return text;
        }
    }

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get("https://elpais.com/opinion/");
        handleCookies(driver, wait);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("article")));

        // Collect first 5 article URLs
        List<WebElement> articleLinks = driver.findElements(
                By.xpath("//article//h2/a[@href]"));

        List<String> firstFiveUrls = new ArrayList<>();
        List<String> spanishTitles = new ArrayList<>();
        List<String> englishTitles = new ArrayList<>();

        for (WebElement link : articleLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("elpais.com")) {
                firstFiveUrls.add(href);
            }
            if (firstFiveUrls.size() == 5) break;
        }

        System.out.println("========= FIRST 5 ARTICLES =========");

        for (int i = 0; i < firstFiveUrls.size(); i++) {

            driver.get(firstFiveUrls.get(i));
            handleCookies(driver, wait);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

            String title = driver.findElement(By.tagName("h1")).getText();
            spanishTitles.add(title);

            System.out.println("\nArticle " + (i + 1));
            System.out.println("Spanish Title: " + title);

            // Extract FULL Spanish content
            List<WebElement> paragraphs = driver.findElements(By.cssSelector("article p"));
            StringBuilder content = new StringBuilder();

            for (WebElement p : paragraphs) {
                content.append(p.getText()).append("\n");
            }

            System.out.println("\nFull Spanish Content:\n");
            System.out.println(content.toString());

            // Download cover image if exists
            try {
                WebElement image = driver.findElement(By.cssSelector("figure img"));
                String imgUrl = image.getAttribute("src");

                if (imgUrl != null && !imgUrl.isEmpty()) {
                    InputStream in = new URL(imgUrl).openStream();
                    Files.copy(in, Paths.get("image_" + i + ".jpg"),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Image downloaded.");
                }
            } catch (Exception e) {
                System.out.println("No image found.");
            }
        }

        // Translate Titles
        System.out.println("\n========= TRANSLATED TITLES =========");

        for (String t : spanishTitles) {
            String translated = translateToEnglish(t);
            englishTitles.add(translated);
            System.out.println(translated);
        }

        // Find repeated words > 2 in translated headers
        Map<String, Integer> headerWordCount = new HashMap<>();

        for (String title : englishTitles) {
            String[] words = title.toLowerCase()
                    .replaceAll("[^a-zA-Z ]", "")
                    .split("\\s+");

            for (String word : words) {
                if (word.length() > 2) {
                    headerWordCount.put(word,
                            headerWordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        System.out.println("\n========= REPEATED WORDS (>2 TIMES) =========");

        for (Map.Entry<String, Integer> entry : headerWordCount.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        }

        driver.quit();
    }
}
