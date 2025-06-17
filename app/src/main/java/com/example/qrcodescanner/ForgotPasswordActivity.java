package com.example.qrcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText emaile;
    Button resete;
    FirebaseAuth myAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emaile= (EditText) findViewById(R.id.email);
        resete= (Button) findViewById(R.id.res);
        myAuth=FirebaseAuth.getInstance();
        resete.setOnClickListener(view -> resetUser());
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
    }

    private void resetUser() {
        String email = emaile.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please Enter Email To Reset Password", Toast.LENGTH_SHORT).show();
        } else {
            myAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(this, "Login Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}