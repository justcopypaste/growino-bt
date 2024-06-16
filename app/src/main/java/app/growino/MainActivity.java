package app.growino;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 2;

    private BluetoothManager bluetoothManager;
    private ListView deviceListView;
    private ArrayAdapter<String> deviceListAdapter;

    // Message types sent from the BluetoothManager to the UI Handler
    public static final int MESSAGE_CONNECTED = 1;
    public static final int MESSAGE_READ = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = new BluetoothManager(handler);

        deviceListView = findViewById(R.id.deviceListView);
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceListAdapter);

        // Check if Bluetooth is supported
        if (!bluetoothManager.isBluetoothSupported()) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            finish();
            return;
        }

        // Request Bluetooth permissions
        requestBluetoothPermissions();

        // Request access to fine location for scanning
        requestLocationPermission();
    }

    // Request Bluetooth permissions
    private void requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth permissions granted, enable Bluetooth
            enableBluetooth();
        }
    }

    // Request access to fine location for scanning
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permissions granted, enable Bluetooth
                    enableBluetooth();
                } else {
                    Log.e(TAG, "Bluetooth permissions denied");
                    // Handle permission denied
                }
                break;
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Fine location permission granted, proceed with scanning
                } else {
                    Log.e(TAG, "Location permissions denied");
                    // Handle permission denied
                }
                break;
        }
    }

    // Enable Bluetooth
    private void enableBluetooth() {
        if (!bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.enableBluetooth(this);
        }

        scanDevices();
    }

    private void scanDevices() {
        // Scan for ESP32 devices
        List<BluetoothDevice> esp32Devices = bluetoothManager.getDevices();

        deviceListAdapter.clear();
        for (BluetoothDevice device : esp32Devices) {
            deviceListAdapter.add(device.getName());
        }

        // Handle device selection
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceName = (String) parent.getItemAtPosition(position);
            for (BluetoothDevice device : esp32Devices) {
                if (device.getName() != null && device.getName().equals(deviceName)) {
                    bluetoothManager.connectToDevice(device);
                    break;
                }
            }
        });

//        new Handler().postDelayed(this::scanDevices, 1000);
    }

    // Handler for receiving messages from BluetoothManager
    private final Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case MESSAGE_CONNECTED:
                BluetoothSocket socket = (BluetoothSocket) msg.obj;
                // Device is connected, handle further actions (e.g., send/receive data)
                break;
            case MESSAGE_READ:
                byte[] buffer = (byte[]) msg.obj;
                int bytesRead = msg.arg1;
                // Handle received data
                break;
        }
        return false;
    });
}
