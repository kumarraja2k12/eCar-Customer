package com.example.blegattclient.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.example.blegattclient.BaseActivity;
import com.example.blegattclient.R;
import com.example.blegattclient.services.IServiceCallback;
import com.example.blegattclient.services.IoTService;
import com.example.blegattclient.services.responses.AddReadingResponse;
import com.example.blegattclient.services.responses.ErrorResponse;
import com.example.blegattclient.storage.preferences.Preferences;

public class RegisterOrEnterVehicleNumberActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_register_or_enter_vehicle_number);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBar.setTitle("Choose Customer Type");
    }

    private void verifyVehicle(final String vehicleNumber) {

        showProgressDialog("Please wait...");
        IoTService.getInstance(getApplicationContext()).VerifyExistingVehicle(vehicleNumber, new IServiceCallback() {
            @Override
            public void OnCompleted(Object response) {
                hideProgressDialog();
                if(((AddReadingResponse)response).Status.equalsIgnoreCase("Success")) {
                    Preferences preferences = Preferences.getInstance(getApplicationContext());
                    boolean result = preferences.writeVehicleNumber(vehicleNumber);
                    hideProgressDialog();
                    showDialog("Success", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent i = new Intent(RegisterOrEnterVehicleNumberActivity.this, MenuActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                } else {
                    showDialog(((AddReadingResponse)response).Status);
                }
            }

            @Override
            public void onError(Object error) {
                hideProgressDialog();
                try {
                    String message = ((ErrorResponse)error).message;
                    showDialog(message);
                } catch (Exception ex){} }
        });
    }

    public void NewCustomer(View view) {
        Intent i = new Intent(RegisterOrEnterVehicleNumberActivity.this, RegisterActivity.class);
        startActivity(i);
        finish();
    }

    public void ExistingCustomer(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter Vehicle Number");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String vehicleNumber= input.getText().toString();
                if(vehicleNumber.isEmpty()) {
                    showLongToast("Please provide Vehicle Number!");
                    return;
                }
                verifyVehicle(vehicleNumber);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}