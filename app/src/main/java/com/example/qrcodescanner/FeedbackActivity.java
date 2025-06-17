package com.example.qrcodescanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FeedbackActivity extends AppCompatActivity {

    EditText feedbackInput;
    Button submitBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackInput = findViewById(R.id.et_feedback);
        submitBtn = findViewById(R.id.btn_submit_feedback);

        submitBtn.setOnClickListener(v -> {
            String feedback = feedbackInput.getText().toString().trim();
            if (TextUtils.isEmpty(feedback)) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            saveFeedbackToFirebase(feedback);
        });
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
