package com.ren.tutornearme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Completable.timer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action() {
            @Override
            public void run() throws Exception {
                Toast.makeText(SplashScreenActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }
}