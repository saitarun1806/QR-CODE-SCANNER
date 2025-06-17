package com.example.qrcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText emaile,passe;
    Button loginb;
    TextView forgotp , signup;
    FirebaseAuth myAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emaile= (EditText) findViewById(R.id.email);
        passe= (EditText) findViewById(R.id.password);
        loginb= (Button) findViewById(R.id.logine);
        forgotp= (TextView) findViewById(R.id.forgot);
        signup= (TextView) findViewById(R.id.signup);
        myAuth=FirebaseAuth.getInstance();
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        loginb.setOnClickListener(view ->loginUser());
        forgotp.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class)));
        signup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this,RegisterActivity.class)));
    }

    private void loginUser() {
        String email=emaile.getText().toString().trim();
        String password=passe.getText().toString().trim();
        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(this, "Please Fill all Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        myAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(this, "Login Successfull", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ScannerActivity.class));
                finish();
            }else {
                Toast.makeText(this, ""
                        +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}