package com.example.qrcodescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    FirebaseAuth myauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myauth=FirebaseAuth.getInstance();
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        new Handler().postDelayed(() -> {
            if (myauth.getCurrentUser() != null) {
                // User is already logged in, go to ScannerActivity
                startActivity(new Intent(this, ScannerActivity.class));
            } else {
                // User is not logged in, go to LoginActivity
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();
        }, 1000);
    }
}