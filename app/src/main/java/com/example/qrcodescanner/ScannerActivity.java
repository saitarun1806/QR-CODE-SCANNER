package com.example.qrcodescanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {

     PreviewView previewView;
     ImageButton flashButton, flipButton, menuBtn;
     Camera camera;
     ProcessCameraProvider cameraProvider;
     CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
     boolean isFlashOn = false;
     boolean scannedOnce = false;
     ExecutorService cameraExecutor;
     int CAMERA_PERMISSION_CODE = 1001;
    DrawerLayout drawerLayout;
     FirebaseAuth myauth;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.pv);
        flashButton = findViewById(R.id.fb);
        flipButton = findViewById(R.id.flip_camera);
        menuBtn= (ImageButton) findViewById(R.id.menu);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        myauth=FirebaseAuth.getInstance();

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_scan) {
                // Already here
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(ScannerActivity.this, HistoryActivity.class));
            } else if (id == R.id.nav_feedback) {
                startActivity(new Intent(ScannerActivity.this, FeedbackActivity.class));
            } else if (id == R.id.nav_signout) {
            myauth.signOut(); // Logs out the user
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ScannerActivity.this, LoginActivity.class));
            finish();
        }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        cameraExecutor = Executors.newSingleThreadExecutor();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        flashButton.setOnClickListener(view -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                isFlashOn = !isFlashOn;
                camera.getCameraControl().enableTorch(isFlashOn);
                flashButton.setImageResource(isFlashOn ? R.drawable.flashon : R.drawable.flashoff);
            }
        });

        flipButton.setOnClickListener(view -> {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();

                // Turn off flash when flipping the camera
                if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                    isFlashOn = false;
                    camera.getCameraControl().enableTorch(false);
                    flashButton.setImageResource(R.drawable.flashoff);
                }

                // Toggle between front and back cameras
                cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                startCamera();
            }
        });
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer if open
        } else {
            super.onBackPressed(); // Otherwise, do normal back behavior
        }
    }
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (imageProxy.getImage() == null) {
                        imageProxy.close();
                        return;
                    }

                    @OptIn(markerClass = ExperimentalGetImage.class) InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(),
                            imageProxy.getImageInfo().getRotationDegrees());

                    BarcodeScanner scanner = BarcodeScanning.getClient();
                    scanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    if (barcode.getFormat() == Barcode.FORMAT_QR_CODE) {
                                        String rawValue = barcode.getRawValue();
                                        if (rawValue != null && !scannedOnce) {
                                            scannedOnce = true;
                                            runOnUiThread(() -> {
                                                Intent intent = new Intent(ScannerActivity.this, ResultActivity.class);
                                                intent.putExtra("qr_result", rawValue);
                                                startActivity(intent);
                                            });

                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(Throwable::printStackTrace)
                            .addOnCompleteListener(task -> imageProxy.close());
                });

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        scannedOnce = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}