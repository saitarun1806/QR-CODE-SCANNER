package com.example.qrcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    EditText emaile ,passe ,passe2;
    Button reg;
    TextView signin;
    FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emaile= (EditText) findViewById(R.id.email);
        passe= (EditText) findViewById(R.id.password);
        passe2= (EditText) findViewById(R.id.password2);
        signin= (TextView) findViewById(R.id.signin);
        reg= (Button) findViewById(R.id.Reg);
        myAuth=FirebaseAuth.getInstance();
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        signin.setOnClickListener(view -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });

        reg.setOnClickListener(view -> RegisterUser());
    }

    private void RegisterUser() {
        String password = passe.getText().toString().trim();
        String confirmPassword = passe2.getText().toString().trim();
        String email = emaile.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please Fill all Fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = myAuth.getCurrentUser();
                    Toast.makeText(this, "Registration Successfull", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(this, "Registration FAiled" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        }
    }
}