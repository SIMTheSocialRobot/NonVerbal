package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String ROBOT_MODE = "nonverbal.robotFaceMode";
    public static final String BLUETOOTH_SERVICE_NAME = "nonverbal";
    public static final String BLUETOOTH_SERVICE_UUID = "bc8a36fa-761e-4b39-a0e6-376d10d10165";

    final int REQUEST_ENABLE_BT = 1;
    final int ROBOT_FACE = 2;

    public static final int FAILED_BT_CONNECTION = 2;
    public static final int BT_CONNECTION_TIMEOUT = 3;

    BluetoothAdapter bluetoothAdapter;
    RobotMode robotMode;

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
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStateReceiver);
    }

    public void launchRobotFace(View view) {
        if (false) {
            Toast alert = Toast.makeText(
                    getApplicationContext(),
                    String.format("Select a Control Device first."),
                    Toast.LENGTH_SHORT);
            alert.show();
        }
        else {
            Intent intent = new Intent(this, RobotFaceActivity.class);
            intent.putExtra(ROBOT_MODE, robotMode.toString());
            startActivityForResult(intent, ROBOT_FACE);
        }
    }

    public void changeRobotMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.robot_mode_sim:
                if (checked)
                    robotMode = RobotMode.SIM;
                    break;
            case R.id.robot_mode_dumb:
                if (checked)
                    robotMode = RobotMode.DUMBOT;
                    break;
            default:
                robotMode = RobotMode.SIM;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                disableControls();
                showBTRequiredAlert();
            }
        }
        else if (requestCode == ROBOT_FACE) {
            if (resultCode == BT_CONNECTION_TIMEOUT) {
                Toast alert = Toast.makeText(
                        getApplicationContext(),
                        String.format("Bluetooth connection timed out."),
                        Toast.LENGTH_LONG);
                alert.show();
            }
        }
    }

    private void disableControls() {
        ((RadioButton) findViewById(R.id.robot_mode_sim)).setEnabled(false);
        ((RadioButton) findViewById(R.id.robot_mode_dumb)).setEnabled(false);
        ((Button) findViewById(R.id.button)).setEnabled(false);
    }

    private void enableControls() {
        ((RadioButton) findViewById(R.id.robot_mode_sim)).setEnabled(true);
        ((RadioButton) findViewById(R.id.robot_mode_dumb)).setEnabled(true);
        ((Button) findViewById(R.id.button)).setEnabled(true);
    }

    private void showBTRequiredAlert() {
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
                        showBTRequiredAlert();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        disableControls();
                        showBTRequiredAlert();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        enableControls();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        enableControls();
                        break;
                }
            }
        }
    };
}
