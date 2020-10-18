package com.example.blegattclient.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.blegattclient.BaseActivity;
import com.example.blegattclient.R;
import com.example.blegattclient.activities.adapters.MenuAdapter;
import com.example.blegattclient.location.LocationService;
import com.example.blegattclient.models.MainMenuItem;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MenuActivity extends BaseActivity {

    private GridView gridView;
    private List<MainMenuItem> menuItems = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private ArrayList<String> permissions = new ArrayList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        gridView = findViewById(R.id.grid_menu);

        menuItems.add(new MainMenuItem("Scan & Connect", R.mipmap.ic_launcher));
        menuItems.add(new MainMenuItem("History", R.mipmap.ic_launcher));
        menuItems.add(new MainMenuItem("Test", R.mipmap.ic_launcher));


        // Create an object of CustomAdapter and set Adapter to GirdView
        MenuAdapter customAdapter = new MenuAdapter(getApplicationContext(), menuItems);
        gridView.setAdapter(customAdapter);
        // implement setOnItemClickListener event on GridView
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (menuItems.get(position).title.equalsIgnoreCase("Scan & Connect")) {
                    // set an Intent to Another Activity
                    Intent intent = new Intent(MenuActivity.this, DeviceScanActivity.class);
                    startActivity(intent); // start Intent*/
                } else if (menuItems.get(position).title.equalsIgnoreCase("History")) {
                    // set an Intent to Another Activity
                    Intent intent = new Intent(MenuActivity.this, HistoryActivity.class);
                    startActivity(intent); // start Intent*/
                } else if (menuItems.get(position).title.equalsIgnoreCase("Test")) {
                    // set an Intent to Another Activity
                    Intent intent = new Intent(MenuActivity.this, TestActivity.class);
                    startActivity(intent); // start Intent*/
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(checkLocationPermissions()) {
            if(checkProviders()) {
                startLocationService();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if(locationService != null) locationService.stopListener();
    }

    //private LocationService locationService;
    Runnable codeRunnable;
    private void startLocationService() {
        //locationService = new LocationService(MenuActivity.this);
        // use this to start and trigger a service
        Intent i= new Intent(MenuActivity.this, LocationService.class);
        startService(i);

        showProgressDialog("Fetching location...");
        final Handler handler = new Handler();
        codeRunnable = new Runnable() {
            int counter = 0;
            @Override
            public void run() {
                showProgressDialog();
                if(LocationService.getCurrentLocation() == null && counter == 30) {
                    hideProgressDialog();
                    showDialog("Not able to retrieve location information", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                } else {
                    if(LocationService.getCurrentLocation() == null) {
                        counter++;
                        handler.postDelayed(codeRunnable, 500);
                    } else {
                        hideProgressDialog();
                    }
                }
            }
        };
        handler.post(codeRunnable);
    }

    private boolean checkLocationPermissions() {
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int counter = 0;
            for(String permission : permissions) {
                 counter += (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)? 1 : 0;
            }

            if(counter == permissions.size()) return true;

            requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, ALL_PERMISSIONS_RESULT);
            return false;
        }
        return true;
    }

    private boolean checkProviders() {

        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);

        // get GPS status
        boolean checkGPS = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // get network provider status
        boolean checkNetwork = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!checkGPS || !checkNetwork) {
            showSettingsAlert();
            return false;
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                final ArrayList<String> permissionsRejected = new ArrayList<String>();
                for (String perms : permissions) {
                    if (!(ContextCompat.checkSelfPermission (MenuActivity.this, perms) == PackageManager.PERMISSION_GRANTED)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (!permissionsRejected.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showDialog("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                } else {
                    if(checkProviders()) {
                        startLocationService();
                    }
                }

                break;
        }

    }

    public void showSettingsAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        alertDialog.show();
    }
}
