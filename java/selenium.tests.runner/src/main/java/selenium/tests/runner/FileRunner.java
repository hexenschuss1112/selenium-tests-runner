package  selenium.tests.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRunner {

    /**
     * ブラウザの種類
     */
    public enum DriverType {
        CHROME, FIREFOX
    };

    /**
     * 
     */
    private static List<String> keyList = Arrays.asList("id", "name", "css", "classname", "xpath");

    /**
     * ロガー
     */ 
    private Logger log = LoggerFactory.getLogger(FileRunner.class);

    /**
     * 結果出力先
     */
    private File dest;

    /**
     * WebDriver
     */
    private EventFiringWebDriver driver;

    private WebDriverWait driverWait;

    /**
     * ブラウザのサイズ
     */
    private Dimension orgBrowserDimension;

    /**
     * 日付
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * seleniumのjsonファイルをパースしたもの
     */
    private JsonNode root;

    /**
     * テスト結果の出力先のパスを設定します。
     * 
     * @param dirPath 出力先のパス
     */
    public void setDestDir(Path dirPath) {
        log.trace("setDestDir dirPath is {}", dirPath.toString());
        this.dest = dirPath.toFile();
    }

    /**
     * ブラウザを指定します。
     * デフォルトはgoogle chromeです。
     * 
     * @param driverType WebDriver名
     * @param driverPath driverの配置場所
     */
    public void setWebDriver(DriverType driverType, String driverPath) {
        log.trace("setWebDriver driverType is {} driverPath is {}", driverType.toString(), driverPath);
        switch (driverType) {
        case FIREFOX:
            System.setProperty("webdriver.gecko.driver", driverPath);
            FirefoxOptions ffOptions = new FirefoxOptions();
            ffOptions.setHeadless(true);
            ffOptions.addArguments("--lang=ja_JP");
            ffOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            ffOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            ffOptions.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
            ffOptions.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT, true);
            this.driver = new EventFiringWebDriver(new FirefoxDriver(ffOptions));
            break;
        case CHROME:
        default:
            // /Users/yasuhisa/Desktop/selenium-tests-runner/java/selenium.tests.runner/drivers/chromedriver
            System.setProperty("webdriver.chrome.driver", driverPath);
            ChromeOptions chOptions = new ChromeOptions();
            chOptions.setHeadless(true);
            chOptions.addArguments("--lang=ja_JP");
            chOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            chOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            chOptions.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
            chOptions.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT, true);
            this.driver = new EventFiringWebDriver(new ChromeDriver(chOptions));
            break;
        }

        EventListener eventListener = new EventListener();
        this.driver.register(eventListener);
    }

    /**
     * ブラウザのサイズを設定します。
     * @param width
     * @param height
     */
    public void setBrowserSize(int width, int height) {
        log.trace("setBrowserSize width={}, height={}", width, height);
        this.orgBrowserDimension = new Dimension(width, height);
    }

    /**
     * seleniumのテストファイルを読み込みます。
     * @param filePath
     * @throws JsonProcessingException
     * @throws IOException
     */
    public void load(Path filePath) throws JsonProcessingException, IOException {
        log.trace("load filePath is {}", filePath.toString());
        ObjectMapper mapper = new ObjectMapper();
        this.root = mapper.readTree(filePath.toFile());
    }

    /**
     * テストファイルを実行します。
     * 
     * @throws Throwable
     */
    public void execute() throws Throwable {
        log.trace("execute");
        log.info("execute start");
        if (this.root == null || this.driver == null || this.dest == null) {
            return;
        }
        init();
        try {
            String host = this.root.get("url").asText();
            for (JsonNode json : this.root.get("tests").findValue("commands")) {

                this.driver.manage().window().setSize(this.orgBrowserDimension);
                String command = json.get("command").asText();
                String target = null;
                for (JsonNode text : json.get("targets")) {
                    text = text.get(0);
                    String[] keyValue = text.asText().split("=");

                    if (keyList.contains(keyValue[0].toLowerCase())) {
                        target = text.asText();
                        break;
                    }
                }
                target = (target == null) ? json.get("target").asText() : target;
                String value = json.get("value").asText();
                switch (command) {
                    case "open":
                        open(host);
                        break;
                    case "type":
                        type(target, value);
                        break;
                    case "click":
                        click(target);
                        break;
                    case "verifyText":
                        Files.copy(this.screenshot(), new File(dest, String.format("screenshot_%s.png", this.sdf.format(new Date()))));
                        verifyText(target, value);
                        break;
                    default:
                        log.trace("unmatch command is {}", command);
                        break;
                }
            }
        } catch(Throwable e) {
            log.error("exception", e);
            throw e;
        } finally {
            finish();
            log.info("execute finish");
        }
    }

    private void init() {
        log.trace("init");
        if (this.dest.exists()) {
            this.dest.delete();
        }
        this.dest.mkdir();
        // 画面上の項目が取れない場合は、描画完了まで最大5秒待つ
        this.driverWait = new WebDriverWait(driver, 5);
        this.driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        this.driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
        this.driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
     }

    private void finish() {
        log.trace("finish");
//        this.driver.close();
        this.driver.quit();
    }

    private void open(String url) {
        log.trace("open url is {}", url);
        this.driver.get(url);
    }

    private void type(String target, String value) {
        log.trace("type target is {} value is {}", target, value);
        WebElement el = getWebElement(target);
        el.sendKeys(value);
    }

    private void click(String target) {
        log.trace("click target is {}", target);
        WebElement el = getWebElement(target);
        el.click();
    }

    private void back() {
        log.trace("back");
        this.driver.navigate().back();
    }

    private void refresh() {
        log.trace("refresh ");
        this.driver.navigate().refresh();
    }

    private void typeAddressbar(String url) {
        log.trace("typeAddressbar url is {}", url);
        this.driver.navigate().to(url);
    }

    private void verifyText(String target, String value) {
        log.trace("verifyText target is {}, value is {}", target, value);
        WebElement el = getWebElement(target);
        String text = el.getText().replaceAll(System.getProperty("line.separator"), "\\\\n");
        if (!value.equals(text)) {
            throw new AssertionError(String.format("expected:<%s> but was:<%s>", value, text));
        }
    }

    private File screenshot() {
        log.trace("screenshot");
        JavascriptExecutor jexec = JavascriptExecutor.class.cast(this.driver);
//        String width = jexec.executeScript("return document.body.scrollWidth").toString();
//        String height = jexec.executeScript("return document.body.scrollHeight").toString();
        String width = jexec.executeScript("return document.documentElement.scrollWidth").toString();
        String height = jexec.executeScript("return document.documentElement.scrollHeight").toString();
        Dimension dimension = new Dimension(Integer.valueOf(width) + 100, Integer.valueOf(height) + 100);
        this.driver.manage().window().setSize(dimension);
        TakesScreenshot sshot = TakesScreenshot.class.cast(this.driver);
        File ret = sshot.getScreenshotAs(OutputType.FILE); 
        this.driver.manage().window().setSize(this.orgBrowserDimension);
        return ret;
    }

    private WebElement getWebElement(String target) {
        log.trace("getWebElement target is {}", target);
        int p = target.indexOf("=");
        String type = target.substring(0, p).toLowerCase();
        String element = target.substring(p + 1);
        By by = null;
        switch(type) {
            case "id":
                by = By.id(element);
                break;
            case "name":
                by = By.name(element);
                break;
            case "css":
                by = By.cssSelector(element);
                break;
            case "classname":
                by = By.className(element);
                break;
            case "xpath":
                by = By.xpath(element);
                break;
            default:
                break;
        }
        this.driverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
        return this.driver.findElement(by);
    }
}