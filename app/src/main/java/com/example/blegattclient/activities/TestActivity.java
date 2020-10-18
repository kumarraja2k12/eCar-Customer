package com.example.blegattclient.activities;

import android.os.Bundle;

import com.example.blegattclient.BaseActivity;
import com.example.blegattclient.models.ReadingModel;
import com.example.blegattclient.notifications.NotificationProvider;
import com.example.blegattclient.services.IServiceCallback;
import com.example.blegattclient.services.IoTService;
import com.example.blegattclient.services.requests.RegisterVehicleRequest;
import com.example.blegattclient.services.responses.AddReadingResponse;
import com.example.blegattclient.services.responses.ErrorResponse;
import com.example.blegattclient.services.responses.RegisterVehicleResponse;
import com.example.blegattclient.storage.preferences.Preferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import android.view.View;
import android.view.Window;

import com.example.blegattclient.R;

import java.util.Date;
import java.util.Random;

public class TestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_test);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBar.setTitle("Test screen");
    }

    public void Register(View view) {

        RegisterVehicleRequest request = new RegisterVehicleRequest("Kumar Donthamsetti",
                "9848098480",
                "DK14VP" + new Random().nextInt(10000),
                "AUDI");

        showProgressDialog("Please wait...");
        IoTService.getInstance(getApplicationContext()).RegisterVehicle(request, new IServiceCallback() {
            @Override
            public void OnCompleted(Object response) {
                RegisterVehicleResponse registeredVehicle = (RegisterVehicleResponse)response;
                Preferences preferences = Preferences.getInstance(getApplicationContext());
                boolean result = preferences.writeVehicleNumber(registeredVehicle.assetId);
                result = preferences.writeThresholds(registeredVehicle.thresholds);
                hideProgressDialog();
                showDialog("Success");
            }

            @Override
            public void onError(Object response) {
                try {
                    //((ErrorResponse)response).message
                    String message = ((ErrorResponse)response).message;
                    showDialog(message, null);
                } catch (Exception ex) {
                    showDialog("Unknown error", null);
                }
            }
        });

    }

    public void FluidReading(View view) {

        final ReadingModel readingModel = new ReadingModel();
        readingModel.timestamp = String.valueOf(new Date().getTime());
        readingModel.value = "1290";
        readingModel.valueType = "level";
        readingModel.vehicleNumber = "TS07GE0208";
        readingModel.latitude =  "17.432";
        readingModel.longitude = "72.234";

        showProgressDialog("Please wait...");
        IoTService.getInstance(getApplicationContext()).AddReading(readingModel.getReadingRequest(), new IServiceCallback() {
            @Override
            public void OnCompleted(Object response) {
                hideProgressDialog();
                if(((AddReadingResponse)response).Status.equalsIgnoreCase("Success")) {
                    showDialog("Success");
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

    public void CoReading(View view) {

        final ReadingModel readingModel = new ReadingModel();
        readingModel.timestamp = String.valueOf(new Date().getTime());
        readingModel.value = "1416";
        readingModel.valueType = "ppm";
        readingModel.vehicleNumber = "TS07GE0208";
        readingModel.latitude =  "17.432";
        readingModel.longitude = "72.234";

        showProgressDialog("Please wait...");
        IoTService.getInstance(getApplicationContext()).AddReading(readingModel.getReadingRequest(), new IServiceCallback() {
            @Override
            public void OnCompleted(Object response) {
                hideProgressDialog();
                if(((AddReadingResponse)response).Status.equalsIgnoreCase("Success")) {
                    showDialog("Success");
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

    public void Notification(View view) {
        NotificationProvider.sendLocalNotification(getApplicationContext(), "Attention", "Vehicle status critical!");
    }
}