# Selenium Test Suite – ElPais Scraper

This directory contains the automated test cases for the ElPais Web Scraper project. These tests validate browser automation behavior across multiple platforms using Selenium, TestNG, Maven, and BrowserStack.

---

## 📂 Test File

### `TestSelenium.java`

This file:

* Executes Selenium-based browser automation
* Runs tests across multiple browsers and operating systems
* Supports parallel execution via TestNG
* Integrates with BrowserStack for cross-platform testing

---

## ⚙️ Test Configuration

Test execution is controlled using the `testng.xml` file located in the project root.

### Supported Environments

* Chrome – Windows 11
* Edge – Windows 11
* Chrome – macOS Monterey
* Safari – macOS Monterey
* Chrome – Android (Google Pixel 7)

Parallel execution is enabled using:

```
parallel="tests"
thread-count="5"
```

---

## 🚀 How to Run Tests

Run the following command from the project root:

```
mvn clean test
```

---

## 📊 Test Reports

After execution, reports are generated in:

```
target/surefire-reports/
```

Each browser execution produces a separate report when running in parallel mode.

---

## 🧩 Tech Stack

* Java
* Selenium WebDriver
* TestNG
* Maven
* BrowserStack

---

Author: Babita Gupta
Project: ElPais Automation & Scraper
