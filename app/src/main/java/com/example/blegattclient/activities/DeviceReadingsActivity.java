package com.example.blegattclient.activities;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.akaita.android.circularseekbar.CircularSeekBar;
import com.example.blegattclient.BaseActivity;
import com.example.blegattclient.R;
import com.example.blegattclient.ble.BluetoothLeService;
import com.example.blegattclient.ble.GattUtils;
import com.example.blegattclient.location.LocationService;
import com.example.blegattclient.models.ReadingModel;
import com.example.blegattclient.services.IServiceCallback;
import com.example.blegattclient.services.IoTService;
import com.example.blegattclient.services.responses.AddReadingResponse;
import com.example.blegattclient.storage.db.DBHelper;
import com.example.blegattclient.storage.preferences.Preferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceReadingsActivity extends BaseActivity {

    private final static String TAG = DeviceReadingsActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private String vehicleNumber;
    private DBHelper dbHelper;

    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                showLongToast("Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                // Show all the supported services and characteristics on the user interface.
                verifyServiceAndCharacteristics(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                try {
                    if(intent.hasExtra(BluetoothLeService.EXTRA_DATA)) {
                        String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                        String value = null;
                        String valueType = null;
                        if (intent.hasExtra(BluetoothLeService.EXTRA_UUID)) {
                            if (GattUtils.isFluidLevelUUID(intent.getStringExtra(BluetoothLeService.EXTRA_UUID))) {
                                Log.d("DeviceReadingsActivity:", "Fluid level data - " + data);
                                data = data.trim();
                                value = String.valueOf(Integer.parseInt(data, 16));
                                valueType = "level";
                                data = "Fluid level data: " + value;
                                processReading(value, valueType);
                            }

                            if (GattUtils.isCOLevelUUID(intent.getStringExtra(BluetoothLeService.EXTRA_UUID))) {
                                data = data.trim();
                                value = String.valueOf(Integer.parseInt(data, 16));
                                valueType = "ppm";
                                data = "CO level data: " + value;
                                processReading(value, valueType);
                            }
                        }
                        displayData(data, value, valueType);
                    }
                } catch (Exception ex) { /*Do nothing*/}
            }
        }
    };

    private String removeNonDigits(final String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return str.replaceAll("/[^0-9]/g", "");
    }

    private void processReading(String value, String valueType) {

        final ReadingModel readingModel = new ReadingModel();
        readingModel.timestamp = String.valueOf(new Date().getTime());
        readingModel.value = value;
        readingModel.valueType = valueType;
        readingModel.vehicleNumber = vehicleNumber;
        if(LocationService.getCurrentLocation() != null) {
            readingModel.latitude =  String.valueOf(LocationService.getCurrentLocation().getLatitude());
            readingModel.longitude = String.valueOf(LocationService.getCurrentLocation().getLongitude());
        }
        dbHelper.insertReading(readingModel);

        IoTService.getInstance(getApplicationContext()).AddReading(readingModel.getReadingRequest(), new IServiceCallback() {
            @Override
            public void OnCompleted(Object response) {
                if(((AddReadingResponse)response).Status.equalsIgnoreCase("Success")) {
                    dbHelper.updateReading(1, readingModel.timestamp);
                }
            }

            @Override
            public void onError(Object error) { /*We can sync failed records*/ }
        });
    }

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_readings);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        vehicleNumber = Preferences.getInstance(getApplicationContext()).readVehicleNumber();
        dbHelper = DBHelper.getInstance(getApplicationContext());

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        ((TextView) findViewById(R.id.vehicle_number_activity_device_reading)).setText(Preferences.getInstance(getApplicationContext()).readVehicleNumber());

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBar.setTitle(mDeviceName + " - Readings");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        /*if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            /*case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;*/
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private void displayData(final String data, final String value, final String valueType) {
        if (data != null && !data.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDataField.setText(
                            simpleDateFormat.format(new Date()) + ": " +
                                    data +
                                    System.getProperty("line.separator") +
                                    mDataField.getText());

                    if(valueType == "ppm") {
                        int min = 0;
                        int max = 50;
                        int valueInt = Integer.parseInt(value);

                        CircularSeekBar seekBar = ((CircularSeekBar)findViewById(R.id.seek_bar_co_level));
                        seekBar.setProgress(Float.parseFloat(value));
                        seekBar.setProgressText(value);
                        if(valueInt > 20 && valueInt < 30) {
                            //warning
                            seekBar.setRingColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_dark));
                        } else if(valueInt < 20) {
                            //normal
                            seekBar.setRingColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_dark));

                        } else {
                            //critical
                            seekBar.setRingColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                        }

                    }

                    if(valueType == "level") {
                        int min = 0;
                        int max = 12;
                        int valueInt = Integer.parseInt(value);

                        CircularSeekBar seekBar = ((CircularSeekBar)findViewById(R.id.seek_bar_fluid_level));
                        seekBar.setProgress(Float.parseFloat(value));
                        seekBar.setProgressText(value);
                        if(valueInt > 3 && valueInt <= 6) {
                            //warning
                            seekBar.setRingColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_dark));
                        } else if(valueInt <= 3) {
                            //critical
                            seekBar.setRingColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));

                        } else {
                            //normal
                            seekBar.setRingColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_dark));
                        }
                    }
                }
            });
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void verifyServiceAndCharacteristics(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            //if (GattUtils.contains(uuid))
            {
                //Service discovered
                //showShortToast(GattUtils.lookup(uuid) + " discovered..");
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();

                    if (GattUtils.contains(uuid))
                    {
                        mBluetoothLeService.setCharacteristicNotification(
                                gattCharacteristic, true);
                        characteristics.add(gattCharacteristic);
                        if(!handlerStarted) { readCharacteristicsPeriodically();}
                    }
                }
            }
        }
    }

    private List<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();
    private boolean handlerStarted;
    private Handler handler;
    private Runnable runnableCode;
    private void readCharacteristicsPeriodically() {
        if(!handlerStarted) {
            handler = new Handler();
            runnableCode = new Runnable() {
                    @Override
                    public void run() {

                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            if(characteristic != null) {
                                mBluetoothLeService.readCharacteristic(characteristic);
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {}
                            }
                        }
                        handler.postDelayed(this, (characteristics.size() <= 1) ? 500 : 5000);
                    }
                };
            handler.post(runnableCode);
            handlerStarted = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(handler != null && runnableCode != null) {
            try {
                handler.removeCallbacks(runnableCode);
            } catch (Exception ex) {}
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}