package com.example.qrcodescanner;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    TextView tvResult, optext, emailtext, dialtext, ettext, smstext;
    ImageButton btnOpen, btnShare, btnCopy, arrow, dial, email, sms, sendt;
    String qrResult;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResult = findViewById(R.id.tv_result);
        btnOpen = findViewById(R.id.btn_open);
        btnShare = findViewById(R.id.btn_share);
        btnCopy = findViewById(R.id.btn_copy);
        arrow = findViewById(R.id.arrow);
        optext = findViewById(R.id.optext);
        emailtext = findViewById(R.id.emailtext);
        email = findViewById(R.id.email);
        dialtext = findViewById(R.id.dialtext);
        dial = findViewById(R.id.dial);
        ettext = findViewById(R.id.ettext);
        smstext = findViewById(R.id.smstext);
        sms = findViewById(R.id.sms);
        sendt = findViewById(R.id.send);

        tvResult.setMovementMethod(LinkMovementMethod.getInstance());
        tvResult.setLinkTextColor(Color.parseColor("#FFFFFF"));
        qrResult = getIntent().getStringExtra("qr_result");

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color2));
        arrow.setOnClickListener(v -> onBackPressed());

        if (qrResult == null) qrResult = "";
        tvResult.setText(qrResult);

        if (sendsms(qrResult)) {
            sms.setVisibility(View.VISIBLE);
            smstext.setVisibility(View.VISIBLE);
        } else {
            sms.setVisibility(View.GONE);
            smstext.setVisibility(View.GONE);
        }

        if (canOpen(qrResult)) {
            btnOpen.setVisibility(View.VISIBLE);
            optext.setVisibility(View.VISIBLE);
        } else {
            btnOpen.setVisibility(View.GONE);
            optext.setVisibility(View.GONE);
        }

        if (emailopen(qrResult)) {
            emailtext.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
        } else {
            emailtext.setVisibility(View.GONE);
            email.setVisibility(View.GONE);
        }

        if (dialopen(qrResult)) {
            dial.setVisibility(View.VISIBLE);
            dialtext.setVisibility(View.VISIBLE);
        } else {
            dial.setVisibility(View.GONE);
            dialtext.setVisibility(View.GONE);
        }

        if (sendemail(qrResult)) {
            sendt.setVisibility(View.VISIBLE);
            ettext.setVisibility(View.VISIBLE);
        } else {
            sendt.setVisibility(View.GONE);
            ettext.setVisibility(View.GONE);
        }

        btnOpen.setOnClickListener(v -> openQrContent(qrResult));
        sendt.setOnClickListener(view -> openQrContent(qrResult));
        sms.setOnClickListener(view -> openQrContent(qrResult));
        dial.setOnClickListener(view -> openQrContent(qrResult));
        email.setOnClickListener(view -> openQrContent(qrResult));
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", qrResult));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });
        btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, qrResult);
            startActivity(Intent.createChooser(share, "Share via"));
        });

        // Save to Firestore
        saveToRealtimeDatabase(qrResult);
    }

    private void saveToRealtimeDatabase(String qrText) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("history");

        String id = ref.push().getKey();
        ScanModel scan = new ScanModel(qrText, System.currentTimeMillis());

        if (id != null) {
            ref.child(id).setValue(scan);
        }
    }


    private boolean sendemail(String text) {
        return text != null && text.trim().replaceAll("\\s+", "").startsWith("mailto:");
    }

    private boolean sendsms(String text) {
        return text != null && text.trim().replaceAll("\\s+", "").startsWith("smsto:");
    }

    private boolean dialopen(String text) {
        if (text == null) return false;
        String cleaned = text.trim().replaceAll("\\s+", "");
        return cleaned.startsWith("tel:");
    }

    private boolean emailopen(String text) {
        if (text == null) return false;
        String cleaned = text.trim().replaceAll("\\s+", "");
        return Patterns.EMAIL_ADDRESS.matcher(cleaned).matches();
    }

    private boolean canOpen(String text) {
        if (text == null) return false;
        String cleaned = text.trim().replaceAll("\\s+", "");
        boolean isPhone = cleaned.matches("^\\+91[6-9]\\d{9}$") || cleaned.matches("^[6-9]\\d{9}$");
        return cleaned.startsWith("upi://pay") || cleaned.startsWith("http") || isPhone;
    }

    private void openQrContent(String text) {
        try {
            if (text == null) return;
            String cleaned = text.trim().replaceAll("\\s+", "");

            if (cleaned.startsWith("upi://pay")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cleaned));
                intent.setPackage("com.google.android.apps.nbu.paisa.user");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    return;
                }
                intent.setPackage("com.phonepe.app");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    return;
                }
                intent.setPackage("net.one97.paytm");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    return;
                }
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(cleaned)), "Choose UPI App"));
            } else if (cleaned.startsWith("http")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cleaned)));
            } else if (Patterns.EMAIL_ADDRESS.matcher(cleaned).matches()) {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + cleaned)));
            } else if (cleaned.matches("^[6-9]\\d{9}$")) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91" + cleaned)));
            } else if (cleaned.matches("^\\+91[6-9]\\d{9}$")) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cleaned)));
            } else if (cleaned.startsWith("tel:")) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(cleaned)));
            } else if (cleaned.startsWith("smsto:")) {
                String[] parts = cleaned.split(":", 3);
                String number = parts.length > 1 ? parts[1] : "";
                String message = parts.length > 2 ? parts[2] : "";

                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.setData(Uri.parse("smsto:" + number));
                smsIntent.putExtra("sms_body", message);
                startActivity(smsIntent);
            } else if (cleaned.startsWith("mailto:")) {
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse(cleaned));
                startActivity(mailIntent);
            } else {
                Toast.makeText(this, "Cannot open this content", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
