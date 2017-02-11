package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothControlDevice;

    final int REQUEST_ENABLE_BT = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        } else {
            fetchBluetoothDevices();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStateReceiver);
    }

    public void launchRobotFace(View view) {
        if (bluetoothControlDevice == null) {
            Toast alert = Toast.makeText(
                    getApplicationContext(),
                    String.format("Select a Control Device first."),
                    Toast.LENGTH_SHORT);
            alert.show();
        }

        Intent intent = new Intent(this, RobotFaceActivity.class);

        startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                fetchBluetoothDevices();
            }
            else {
                disableControls();
                showBTORequiredAlert();
            }
        }
    }

    private void disableControls() {
        ((Spinner) findViewById(R.id.bt_device_list)).setEnabled(false);
        ((Button) findViewById(R.id.button)).setEnabled(false);
    }

    private void enableControls() {
        ((Spinner) findViewById(R.id.bt_device_list)).setEnabled(true);
        ((Button) findViewById(R.id.button)).setEnabled(true);
    }

    private void fetchBluetoothDevices() {
        Spinner ctrl = (Spinner) findViewById(R.id.bt_device_list);
        final Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        final List<String> deviceNames = new ArrayList<String>();
        final BluetoothDevice[] devices = deviceSet.toArray(new BluetoothDevice[deviceSet.size()]);

        for (BluetoothDevice device : devices) {
            deviceNames.add(device.getName());
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, deviceNames.toArray(new String[deviceNames.size()]));
        ctrl.setAdapter(adapter);
        ctrl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bluetoothControlDevice = devices[position];
                Toast alert = Toast.makeText(getApplicationContext(), String.format("Control Device set to %s", bluetoothControlDevice.getName()), Toast.LENGTH_SHORT);
                alert.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bluetoothControlDevice = null;
                Toast alert = Toast.makeText(getApplicationContext(), String.format("Control Device cleared"), Toast.LENGTH_SHORT);
                alert.show();
            }
        });
    }

    private void showBTORequiredAlert() {
        Toast alert = Toast.makeText(
                getApplicationContext(),
                String.format("Bluetooth is required."),
                Toast.LENGTH_SHORT);
        alert.show();
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        disableControls();
                        showBTORequiredAlert();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        disableControls();
                        showBTORequiredAlert();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        enableControls();
                        fetchBluetoothDevices();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        enableControls();
                        break;
                }
            }
        }
    };
}
