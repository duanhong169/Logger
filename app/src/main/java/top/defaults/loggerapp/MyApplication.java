package top.defaults.loggerapp;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import timber.log.Timber;
import top.defaults.logger.Logger;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setLevel(Log.VERBOSE);
        Logger.setTagPrefix("MyApplication");
        Timber.plant(new LoggerTree());
    }

    private static class LoggerTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            Logger.logWithTimber(priority, tag, message);
        }
    }
}
