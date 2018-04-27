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

                    new Thread(() -> {
                        for (int i = 0; i < 600; i++) {
                            Timber.v("Logger");
                            Timber.d("Logger");
                            Timber.i("Logger");
                            Timber.w("Logger");
                            Timber.e("Logger");
                            Timber.wtf("Logger");

                            Logger.v("Logger: %s", getClass().getCanonicalName());
                            Logger.d("Logger: %s", getClass().getCanonicalName());
                            Logger.i("Logger: %s", getClass().getCanonicalName());
                            Logger.w("Logger: %s", getClass().getCanonicalName());
                            Logger.e("Logger: %s", getClass().getCanonicalName());
                            Logger.wtf("Logger: %s", getClass().getCanonicalName());

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                });
    }
}
