package com.airmap.airmapsdktest.activities;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected boolean isActive() {
        return !isFinishing() && !isDestroyed();
    }

    protected void showErrorDialog(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);

        builder.show();
    }
}
