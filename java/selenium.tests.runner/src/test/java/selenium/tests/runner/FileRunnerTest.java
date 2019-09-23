package  selenium.tests.runner;

import static org.junit.Assert.fail;

import java.nio.file.Paths;

import org.junit.Test;

import selenium.tests.runner.FileRunner.DriverType;

public class FileRunnerTest {

    @Test
    public void test01() {
        FileRunner fr = new FileRunner();
        fr.setDestDir(Paths.get("./target/testresult"));
//        fr.setWebDriver(DriverType.CHROME, "./drivers/chromedriver");
        fr.setWebDriver(DriverType.FIREFOX, "./drivers/geckodriver");
        fr.setBrowserSize(1024, 768);
        try {
            fr.load(Paths.get("./yahoo.side"));
            fr.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }
}