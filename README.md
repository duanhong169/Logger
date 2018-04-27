# Logger

This is a logger based on the official android.util.Log API and provided the following features:

* Add file & line number info (e.g. MainActivity.java:15) to the log tag, which support to jump to the log line by a click.
* Save logs to file.
* Support formatted `String` directly.
* Open/Close log messages output at runtime (using `adb shell setprop log.tag.YOUR_TAG DEBUG`).
* Integrate with [Timber](https://github.com/JakeWharton/timber).

With these features, the Logger can help you debugging your apps more easily.

## Gradle

> Replace ${latestVersion} with the latest version code. See [releases](https://github.com/duanhong169/Logger/releases).

```
    implementation 'com.github.duanhong169:logger:${latestVersion}'
```

## Usage

### Config Logger (optional)

Use the static methods to config Logger if needed:

```java
    Logger.setLevel(Log.DEBUG);
    Logger.setTagPrefix("MyApplication");
    Logger.setLogFile(Environment.getExternalStorageDirectory().getPath() + "/0/dev/log");
    Logger.setLogFileMaxSizeInMegabytes(10);
```

### Print log

```java
    Logger.v("Logger: %s", variable);
    Logger.d("Logger: %s", variable);
    Logger.i("Logger: %s", variable);
    Logger.w("Logger: %s", variable);
    Logger.e("Logger: %s", variable);
    Logger.wtf("Logger: %s", variable);
```

### Control logcat output

> The setprop will override the log level config.

Use shell commands:

```shell
    adb shell setprop log.tag.MyApplication DEBUG # Open
    adb shell setprop log.tag.MyApplication INFO  # Close
```

For more details, see the sample app.

### License

See the [LICENSE](./LICENSE) file.