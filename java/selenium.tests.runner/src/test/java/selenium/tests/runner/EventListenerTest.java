package  selenium.tests.runner;

import org.junit.Test;

public class EventListenerTest {

    @Test
    public void beforeNavigateTo01() {
        EventListener listener = new EventListener();
        listener.beforeNavigateTo("url", null);
    }

    @Test
    public void afterNavigateTo01() {
        EventListener listener = new EventListener();
        listener.afterNavigateTo("url", null);
    }

    @Test
    public void onException01() {
        EventListener listener = new EventListener();
        listener.onException(new Throwable(), null);
    }

    @Test
    public void beforeScript01() {
        EventListener listener = new EventListener();
        listener.beforeScript("url", null);
    }

    @Test
    public void afterScript01() {
        EventListener listener = new EventListener();
        listener.afterScript("url", null);
    }
}
