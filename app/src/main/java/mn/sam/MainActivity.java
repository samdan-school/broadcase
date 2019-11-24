package mn.sam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TelephonyManager tManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActivityCompat.requestPermissions(this, new String[]{
//                Manifest.permission.SEND_SMS,
//                Manifest.permission.RECEIVE_SMS,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_PHONE_STATE,
//        }, 1);
        setContentView(R.layout.activity_main);

        telP();

        simInfo();

        deviceInfo();

        findViewById(R.id.btnTexMess).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btnTexMess: {
                intent = new Intent(this, MessageText.class);
                break;
            }
        }

        startActivity(intent);
    }

    private void telP() {
        tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tManager.listen(new CustomPhoneStateListener(this),
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY
                        | PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    private class CustomPhoneStateListener extends PhoneStateListener {
        Context mContext;

        CustomPhoneStateListener(Context context) {
            mContext = context;
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            super.onCallForwardingIndicatorChanged(cfi);
            outFile(" - CallForwardingIndicator " + cfi);
        }

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            outFile(" - CallState state " + state + " pn: " + phoneNumber);
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);
            outFile(" - CellInfo " + cellInfo);
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            outFile(" - CellLocation " + location);
        }

        @Override
        public void onDataActivity(int direction) {
            super.onDataActivity(direction);
            outFile(" - DataActivity " + direction);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            outFile(" - ServiceState " + serviceState);
        }

        private void outFile(String info) {
            // TODO write date into phonestate.txt
            // date - info new Date();
            File root = new File(Environment.getExternalStorageDirectory().toString());
            File phoneState = new File(root, "phonestate.txt");
            FileWriter writer;
            String text = new Date().toString() + " " + info + "\n";
            try {
                writer = new FileWriter(phoneState, true);
                writer.append(text);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void simInfo() {
        tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        File root = new File(Environment.getExternalStorageDirectory().toString());
        File simInfo = new File(root, "SIMInfo.txt");
        FileWriter writer;
        Log.i("Hi", Environment.getExternalStorageDirectory().toString() + !simInfo.exists() + " " + simInfo.exists());
        @SuppressLint({"MissingPermission", "HardwareIds"})
        String text = "" +
                "Phone Number\t: " + tManager.getLine1Number() +
                "\nCountry ISO\t\t: " + tManager.getSimCountryIso() +
                "\nOperator Code\t: " + tManager.getSimOperator() +
                "\nOperator Name\t: " + tManager.getSimOperatorName() +
                "\nSim Serial\t\t: " + tManager.getSimSerialNumber();
        if (!simInfo.exists()) {
            try {
                writer = new FileWriter(simInfo);
                writer.append(text);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deviceInfo() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(new DeviceBroadcastReceiver(), intentFilter);
    }
}
