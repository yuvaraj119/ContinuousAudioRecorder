package com.yuva.continuousaudiorecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    Button btStart;
    RelativeLayout activity_main2;
    private static final int REQUEST_CODE_PERMISSIONS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        activity_main2 = (RelativeLayout) findViewById(R.id.activity_main2);
        btStart = (Button) findViewById(R.id.button);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryStart();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void tryStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int checkAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            final int checkStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            final int checkReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkAudio != PackageManager.PERMISSION_GRANTED || checkStorage != PackageManager.PERMISSION_GRANTED ||
                    checkReadStorage != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    showNeedPermissionsMessage();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showNeedPermissionsMessage();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showNeedPermissionsMessage();
                } else {
                    requestPermissions(new String[]{
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSIONS);
                }
            } else {
                start();
            }
        } else {
            start();
        }
    }

    private void showNeedPermissionsMessage() {
        message(getString(R.string.error_no_permissions));
    }

    private void message(String message) {
        final View root = activity_main2;
        if (root != null) {
            final Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    tryStart();
                }
            });
            snackbar.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                boolean userAllowed = true;
                for (final int result : grantResults) {
                    userAllowed &= result == PackageManager.PERMISSION_GRANTED;
                }
                if (userAllowed) {
                    start();
                } else {
                    /*
                     * Cannot show dialog from here
                     * https://code.google.com/p/android-developer-preview/issues/detail?id=2823
                     */
                    showNeedPermissionsMessage();
                }
                break;
            default:
                break;
        }
    }

    public void start() {
        Intent intent = new Intent(MainActivity.this, AudioPlayerActivity.class);
        startActivity(intent);
        finish();
    }

}
