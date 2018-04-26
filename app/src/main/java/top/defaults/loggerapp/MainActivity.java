package top.defaults.loggerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tbruyelle.rxpermissions2.RxPermissions;

import timber.log.Timber;
import top.defaults.logger.Logger;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        Logger.setLogFile(Environment.getExternalStorageDirectory().getPath() + "/0/dev/log");
                    }

                    for (int i = 0; i < 3000; i++) {
                        Timber.v("Smart log");
                        Timber.d("Smart log");
                        Timber.i("Smart log");
                        Timber.w("Smart log");
                        Timber.e("Smart log");
                        Timber.wtf("Smart log");

                        Logger.v("Smart log: %s", getClass().getCanonicalName());
                        Logger.d("Smart log: %s", getClass().getCanonicalName());
                        Logger.i("Smart log: %s", getClass().getCanonicalName());
                        Logger.w("Smart log: %s", getClass().getCanonicalName());
                        Logger.e("Smart log: %s", getClass().getCanonicalName());
                        Logger.wtf("Smart log: %s", getClass().getCanonicalName());
                    }
                });
    }
}
