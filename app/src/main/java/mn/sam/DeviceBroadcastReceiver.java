package mn.sam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DeviceBroadcastReceiver extends BroadcastReceiver {
    //  -төхөөрөмжийг цэнэглэчээс салгахад
//      /Powerdisconnected:
//          Тухайн цагийн мэдээлэл,
//          хэдэн минут цэнэглэсэн талаарх мэдээлэл,
//          хэдэн хувьтай цэнэглэгдснийг тус бүр log файлд бич./
    Calendar chargeStartTime, chargeEndTime;
    float chargeStartPtr, chargeEndPtr;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String text = null;
            boolean defaultTime = true;

            int level, scale;

            switch (Objects.requireNonNull(intent.getAction())) {
                case Intent.ACTION_TIME_CHANGED:
                    if (Calendar.getInstance().get(Calendar.MINUTE) % 5 == 0) {
                        text = "TIME\t\t\t\t:";
                    } else {
                        text = "TimeChanged\t\t\t:";
                    }
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    text = "BootCompleted\t\t:";
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    chargeStartTime = Calendar.getInstance();
                    text = "PowerConnected\t\t:";
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    chargeEndTime = Calendar.getInstance();
                    defaultTime = false;
                    if (chargeStartTime == null) {
                        return;
                    }
                    text = "PowerDisconnected\t: " + chargeEndTime.getTime().toString() +
                            "\n\t\t\t\t\t  minute: " + (chargeEndTime.get(Calendar.MINUTE) - chargeStartTime.get(Calendar.MINUTE)) +
                            "\n\t\t\t\t\t  ptr: " + (chargeEndPtr - chargeStartPtr);
                    chargeStartTime = null;
                    chargeEndTime = null;
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    text = "BatteryChanged\t\t:";
                    level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    if (chargeStartTime == null) {
                        chargeStartPtr = level * 100 / (float) scale;
                    }
                    if (chargeEndTime == null) {
                        chargeEndPtr = level * 100 / (float) scale;
                    }
            }

            if (text != null) {
                outFile(text, defaultTime);
            }
        }
    }

    private void outFile(String info, boolean defaultTime) {
        File root = new File(Environment.getExternalStorageDirectory().toString());
        File deviceInfo = new File(root, "deviceInfo.txt");
        FileWriter writer;
        String text = info + (defaultTime ? " " + new Date().toString() : "") + "\n";
        try {
            writer = new FileWriter(deviceInfo, true);
            writer.append(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
