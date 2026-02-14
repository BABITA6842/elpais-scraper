package com.assignment.tests;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * El País Opinion Section Web Scraper
 * 
 * This test demonstrates:
 * - Web scraping with Selenium
 * - API integration for translation
 * - Text processing and analysis
 * - Cross-browser testing on BrowserStack
 * 
 * @author Assignment Solution
 */
public class TestSelenium {

    WebDriver driver;
    WebDriverWait wait;
    
    // BrowserStack credentials
    public static final String USERNAME = "babita_6G3bX0";
    public static final String AUTOMATE_KEY = "Lzxhn7zxt9s1wCjMs72z";
    public static final String BS_URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";
    
    /**
     * Downloads an image from URL to local file
     */
    public void downloadImage(String urlStr, String fileName) throws Exception {
        java.net.URL url = new java.net.URL(urlStr);
        try (java.io.InputStream in = url.openStream()) {
            java.nio.file.Files.copy(in, java.nio.file.Paths.get(fileName), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✓ Downloaded: " + fileName);
        }
    }
    
    /**
     * Translates Spanish text to English using Google Translate API (RapidAPI)
     */
    public String translateText(String textToTranslate) throws Exception {
        String apiKey = "7251de4895msh27e566282774a49p102145jsnf98af527d9ad";
        
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        
        // Google Translate 113 API format
        String jsonBody = String.format(
            "{\"from\":\"es\",\"to\":\"en\",\"json\":{\"text\":\"%s\"}}", 
            textToTranslate.replace("\"", "\\\"").replace("\n", " ").replace("\r", "")
        );
        
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
            .url("https://google-translate113.p.rapidapi.com/api/v1/translator/json")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-rapidapi-host", "google-translate113.p.rapidapi.com")
            .addHeader("x-rapidapi-key", apiKey)
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (response.isSuccessful()) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                
                // google-translate113 returns: {"trans":{"text":"translated text"}}
                if (jsonObject.has("trans")) {
                    JsonObject trans = jsonObject.getAsJsonObject("trans");
                    if (trans.has("text")) {
                        return trans.get("text").getAsString();
                    }
                }
            }
            
            System.err.println("Translation failed: HTTP " + response.code() + " - " + responseBody);
            return "Translation Error: HTTP " + response.code();
        } catch (Exception e) {
            System.err.println("Translation exception: " + e.getMessage());
            return "Translation Error: " + e.getMessage();
        }
    }
    
    /**
     * Analyzes translated headers to find words repeated more than twice
     */
    public void analyzeRepeatedWords(List<String> headers) {
        Map<String, Integer> wordMap = new HashMap<>();
        
        for (String header : headers) {
            // Skip error messages
            if (header.contains("Translation Error")) continue;
            
            // Split into words, convert to lowercase, remove punctuation
            String[] words = header.toLowerCase()
                                  .replaceAll("[^a-zA-Z ]", "")
                                  .split("\\s+");
            
            for (String word : words) {
                // Only count words longer than 2 characters (skip articles like "a", "an", "the")
                if (word.length() > 2) { 
                    wordMap.put(word, wordMap.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("WORD FREQUENCY ANALYSIS");
        System.out.println("Words repeated MORE than 2 times across all headers:");
        System.out.println("=".repeat(70));
        
        boolean foundRepeats = false;
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.printf("  '%s' - appears %d times%n", entry.getKey(), entry.getValue());
                foundRepeats = true;
            }
        }
        
        if (!foundRepeats) {
            System.out.println("  No words repeated more than 2 times.");
        }
        System.out.println("=".repeat(70));
    }

    /**
     * Setup method - configures WebDriver for different browsers and platforms
     */
    @BeforeMethod
    @Parameters({"browser", "os", "os_version", "deviceName"})
    public void setUp(String browser, 
                      @Optional("") String os, 
                      @Optional("") String os_version, 
                      @Optional("") String deviceName) throws Exception {
        
        MutableCapabilities capabilities = new MutableCapabilities();
        HashMap<String, Object> bstackOptions = new HashMap<>();

        // Configure for desktop or mobile
        if (deviceName != null && !deviceName.isEmpty()) {
            // Mobile device configuration
            bstackOptions.put("deviceName", deviceName);
            bstackOptions.put("osVersion", os_version);
            System.out.println("Testing on Mobile: " + deviceName + " - " + browser);
        } else {
            // Desktop configuration
            bstackOptions.put("os", os);
            bstackOptions.put("osVersion", os_version);
            System.out.println("Testing on Desktop: " + os + " " + os_version + " - " + browser);
        }

        // Safari-specific settings
        if ("safari".equalsIgnoreCase(browser)) {
            bstackOptions.put("browserVersion", "latest");
            bstackOptions.put("seleniumVersion", "4.0.0");
        }

        bstackOptions.put("projectName", "El País Opinion Scraper");
        bstackOptions.put("buildName", "Cross-Browser Testing");
        bstackOptions.put("sessionName", browser + " - " + (deviceName != null && !deviceName.isEmpty() ? deviceName : os));

        capabilities.setCapability("browserName", browser);
        capabilities.setCapability("bstack:options", bstackOptions);

        driver = new RemoteWebDriver(new URL(BS_URL), capabilities);
        
        // Only maximize on desktop browsers
        if (deviceName == null || deviceName.isEmpty()) {
            try {
                driver.manage().window().maximize();
            } catch (Exception e) {
                System.out.println("Note: Could not maximize window");
            }
        }
        
        // Increased wait time for better stability
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    /**
     * Main test method - executes all scraping, translation, and analysis tasks
     */
    @Test
    public void scrapeAndAnalyzeOpinionSection() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STARTING EL PAÍS OPINION SECTION SCRAPER");
        System.out.println("=".repeat(70));
        
        // Step 1: Navigate to El País Opinion section
        System.out.println("\n[1/5] Navigating to El País Opinion section...");
        driver.get("https://elpais.com/opinion/");
        Thread.sleep(3000); // Allow page to fully load (important for Safari)
        
        // Step 2: Verify language is Spanish
        System.out.println("\n[2/5] Verifying website language...");
        String lang = (String) ((JavascriptExecutor) driver)
            .executeScript("return document.documentElement.lang;");
        System.out.println("✓ Website language detected: " + lang);
        
        if (!lang.toLowerCase().startsWith("es")) {
            System.out.println("⚠ Warning: Language might not be Spanish!");
        }

        // Step 3: Scrape first 5 articles
        System.out.println("\n[3/5] Scraping articles from Opinion section...");
        
        // Find article elements using CSS selector (more reliable than XPath)
        List<WebElement> articleElements = wait.until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("article h2 a, article h2.c_h a")
            )
        );
        
        System.out.println("✓ Found " + articleElements.size() + " total articles on page");
        
        List<String> spanishTitles = new ArrayList<>();
        int articlesProcessed = 0;
        int attemptCount = 0;
        
        // Process articles until we have 5 valid ones
        while (articlesProcessed < 5 && attemptCount < articleElements.size()) {
            // Re-fetch elements to avoid stale references
            articleElements = driver.findElements(
                By.cssSelector("article h2 a, article h2.c_h a")
            );
            
            if (attemptCount >= articleElements.size()) break;
            
            WebElement articleLink = articleElements.get(attemptCount);
            attemptCount++;
            
            String title = articleLink.getText();
            
            // Skip if no title or if it's an ad
            if (title == null || title.trim().isEmpty() || 
                title.contains("Publicidad") || title.contains("Advertisement")) {
                continue;
            }
            
            spanishTitles.add(title);
            
            System.out.println("\n" + "-".repeat(70));
            System.out.println("ARTICLE #" + (articlesProcessed + 1));
            System.out.println("-".repeat(70));
            System.out.println("Spanish Title: " + title);
            
            // Try to get article content/description
            try {
                WebElement articleParent = articleLink.findElement(By.xpath("./ancestor::article"));
                List<WebElement> paragraphs = articleParent.findElements(By.tagName("p"));
                
                if (!paragraphs.isEmpty()) {
                    String content = paragraphs.get(0).getText();
                    if (content != null && !content.trim().isEmpty()) {
                        System.out.println("Spanish Content: " + content);
                    }
                }
            } catch (Exception e) {
                System.out.println("Spanish Content: [Content preview not available]");
            }

            // Try to download cover image
            try {
                WebElement articleParent = articleLink.findElement(By.xpath("./ancestor::article"));
                WebElement img = articleParent.findElement(By.tagName("img"));
                String imgUrl = img.getAttribute("src");
                
                if (imgUrl != null && imgUrl.startsWith("http")) {
                    String fileName = "article_" + (articlesProcessed + 1) + "_image.jpg";
                    downloadImage(imgUrl, fileName);
                } else {
                    System.out.println("✗ Image URL invalid or not found");
                }
            } catch (Exception e) {
                System.out.println("✗ No cover image available for this article");
            }
            
            articlesProcessed++;
        }
        
        System.out.println("\n✓ Successfully scraped " + articlesProcessed + " articles");

        // Step 4: Translate headers to English
        System.out.println("\n[4/5] Translating article headers to English...");
        System.out.println("(Using Google Translate API via RapidAPI - waiting 15 seconds between calls)");
        
        List<String> englishTitles = new ArrayList<>();
        
        for (int i = 0; i < spanishTitles.size(); i++) {
            String spanishTitle = spanishTitles.get(i);
            
            // Rate limiting: wait between API calls (except first one)
            if (i > 0) {
                System.out.println("\n⏳ Waiting 15 seconds before next translation...");
                Thread.sleep(15000);
            }
            
            System.out.println("\nTranslating article #" + (i + 1) + "...");
            System.out.println("  Original (ES): " + spanishTitle);
            
            String englishTitle = translateText(spanishTitle);
            englishTitles.add(englishTitle);
            
            System.out.println("  Translated (EN): " + englishTitle);
        }
        
        System.out.println("\n✓ Translation complete!");

        // Step 5: Analyze translated headers for repeated words
        System.out.println("\n[5/5] Analyzing translated headers for word frequency...");
        analyzeRepeatedWords(englishTitles);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST COMPLETED SUCCESSFULLY!");
        System.out.println("=".repeat(70) + "\n");
    }
    
    /**
     * Cleanup method - closes browser after test
     */
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}