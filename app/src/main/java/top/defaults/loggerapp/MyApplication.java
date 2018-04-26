package top.defaults.loggerapp;

import android.app.Application;
import android.util.Log;

import top.defaults.logger.Logger;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setLevel(Log.VERBOSE);
        Logger.setTagPrefix("MyApplication");
    }
}
