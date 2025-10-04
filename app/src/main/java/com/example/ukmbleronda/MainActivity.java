package com.example.ukmbleronda;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<String> deviceList;
    private ListView listViewDevices;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listViewDevices = findViewById(R.id.listViewDevices);
        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listViewDevices.setAdapter(deviceListAdapter);

        // Check if device supports Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Request Bluetooth permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            startBluetooth();
        }

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            // Handle item click, redirect to DeviceDetailsActivity
            String deviceName = deviceListAdapter.getItem(position);
            if (deviceName != null) {
                Toast.makeText(MainActivity.this, "View details of " + deviceName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DeviceDetailsActivity.class);
                intent.putExtra("DEVICE_NAME", deviceName);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Device name is null", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnScan).setOnClickListener(v -> startScan());
    }

    @SuppressLint("MissingPermission")
    private void startBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    private void startScan() {
        deviceList.clear();
        deviceListAdapter.notifyDataSetChanged();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startLeScan(leScanCallback);
            Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(() -> {
                        @SuppressLint("MissingPermission") String deviceName = device.getName();
                        if (deviceName != null && !deviceList.contains(deviceName)) {
                            deviceList.add(deviceName);
                            deviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetooth();
                startScan();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
