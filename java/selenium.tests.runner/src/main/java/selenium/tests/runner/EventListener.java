
package  selenium.tests.runner;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://www.seleniumqref.com/api/java
 */
public class EventListener extends AbstractWebDriverEventListener {

    /**
     * ロガー
     */
    private Logger log = LoggerFactory.getLogger(EventListener.class);

    /**
     * 次画面遷移前
     */
    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {
        log.trace("beforeNavigateTo url is {} driver.currentUrl is {}", url, driver.getCurrentUrl());
    }

    /**
     * 次画面遷移後
     */
    @Override
    public void afterNavigateTo(String url, WebDriver driver){
        log.trace("afterNavigateTo url is {} driver.currentUrl is {}", url, driver.getCurrentUrl());
    }
  
    /**
     * 例外通知前
     */
    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        log.trace("onException exception message is {}", throwable.getMessage());
    }

    /**
     * javascript実行前
     */
    @Override
    public void beforeScript(String url, WebDriver driver) {
        log.trace("beforeScript url is {}", url);
    }

    /**
     * javascript実行後
     */
    @Override
    public void afterScript(String url, WebDriver driver) {
        log.trace("afterScript url is {}", url);
    }
  }