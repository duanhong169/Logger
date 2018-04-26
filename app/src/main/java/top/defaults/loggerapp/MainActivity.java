package top.defaults.loggerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import top.defaults.logger.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.logThreadStart();
        Logger.d("Pretty log");
        Logger.logThreadFinish();
    }
}
