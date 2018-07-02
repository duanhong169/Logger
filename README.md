# Logger

This is a logger based on the official [`android.util.Log`](https://developer.android.com/reference/android/util/Log.html) API and with some additional features:

* Add file & line number info (e.g. MainActivity.java:15) to the log tag, which support to jump to the log line by a click.
* Save logs to file.
* Support formatted `String` directly.
* Open/Close log messages output at runtime (using `adb shell setprop log.tag.YOUR_TAG DEBUG`).
* Integrate with [Timber](https://github.com/JakeWharton/timber).

With these features, the `Logger` can help you debugging your apps more easily.

## Gradle

```
dependencies {
    implementation 'com.github.duanhong169:logger:${latestVersion}'
    ...
}
```

> Replace `${latestVersion}` with the latest version code. See [releases](https://github.com/duanhong169/Logger/releases).

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

### Integrate with Timber

```java

private static class LoggerTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        Logger.logWithTimber(priority, tag, message);
    }
}

Timber.plant(new LoggerTree());
```

### Control logcat output

Use shell commands:

```shell
adb shell setprop log.tag.MyApplication DEBUG # Open
adb shell setprop log.tag.MyApplication INFO  # Close
```

> The setprop will override the log level config.

For more details, see the sample app.

## License

See the [LICENSE](./LICENSE) file.