package com.example.qrcodescanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FeedbackActivity extends AppCompatActivity {

    EditText feedbackInput;
    Button submitBtn;
    FirebaseAuth myauth;
    DrawerLayout drawerLayout;
    ImageView menuBtn;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackInput = findViewById(R.id.et_feedback);
        submitBtn = findViewById(R.id.btn_submit_feedback);
        myauth=FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navView = findViewById(R.id.nav_view);
        menuBtn = findViewById(R.id.menu_btn);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        menuBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

// Set navigation actions here ONCE
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feedback) {
                // Already here
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(FeedbackActivity.this, HistoryActivity.class));
            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(FeedbackActivity.this, ScannerActivity.class));
            } else if (id == R.id.nav_signout) {
                myauth.signOut();
                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(FeedbackActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        submitBtn.setOnClickListener(v -> {
            String feedback = feedbackInput.getText().toString().trim();
            if (TextUtils.isEmpty(feedback)) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            saveFeedbackToFirebase(feedback);
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void saveFeedbackToFirebase(String feedback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("feedback");

        String feedbackId = feedbackRef.push().getKey(); // optional: if you want multiple feedbacks
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", feedback);
        map.put("timestamp", System.currentTimeMillis());

        assert feedbackId != null;
        feedbackRef.child(feedbackId).setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    feedbackInput.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send feedback", Toast.LENGTH_SHORT).show());
    }
}
