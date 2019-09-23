Selenium Tests Runner
====

Selenium IDE でファイル保存をしたテストを、Javaプログラムから実行します。
`verifyText`を行うタイミングでスクリーンショットを取得します。

## Dependency
* java1.8 
* WebDriver（Firefox, google chrome）

## Usage
```java
FileRunner fr = new FileRunner();
fr.setDestDir(Paths.get("./target/testresult"));
fr.setWebDriver(DriverType.FIREFOX, "./drivers/geckodriver");
fr.setBrowserSize(1024, 768);
fr.load(Paths.get("./yahoo.side"));
fr.execute();

```
## Setup

## Licence
Copyright (c) 2019 hexenschuss1112
Released under the MIT license
https://opensource.org/licenses/mit-license.php

## Author
[hexenschuss1112](https://github.com/hexenschuss1112)

# References
[Selenium HQ](https://docs.seleniumhq.org)

[Seleniumクイックリファレンス](https://www.seleniumqref.com/index.html)
