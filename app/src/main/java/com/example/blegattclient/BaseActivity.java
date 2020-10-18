package com.example.blegattclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    public void showProgressDialog() {
        showProgressDialog(null);
    }

    public void showProgressDialog(String message) {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(BaseActivity.this);
            progressDialog.setMessage((message == null) ? "Scanning...." : message);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        if(!progressDialog.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }
    }
    
    public void hideProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        }
    }

    public void showDialog(String message) {
        showDialog(message, null);
    }

    public void showDialog(String message, DialogInterface.OnClickListener listener) {

        final AlertDialog alertDialog1 = new AlertDialog.Builder(
                BaseActivity.this).create();

        // Setting Dialog Title
        alertDialog1.setTitle(R.string.app_name);

        // Setting Dialog Message
        alertDialog1.setMessage(message);

        // Setting OK Button
        alertDialog1.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (listener == null) ? new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        } : listener);

        /* Showing Alert Message */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog1.show();
            }
        });
    }

    public void showLongToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText( BaseActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void showShortToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText( BaseActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void requiredField(TextView text) {
        String colored = "*";
        SpannableStringBuilder builder = new SpannableStringBuilder(text.getText() + colored);

        builder.setSpan(new ForegroundColorSpan(Color.RED), text.getText().length(), builder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setText(builder);
    }
}
