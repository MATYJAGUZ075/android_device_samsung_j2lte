/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.j2lte;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.hardware.usb.UsbManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Panel de optimizaciones del J2.
 *
 * El equipo tiene 814 MB utiles, asi que la RAM es el recurso mas escaso. Este
 * panel deja a mano los ajustes que de otro modo solo se tocan en Opciones de
 * desarrollador, y que ademas hay que cambiar cada vez que se nota lento.
 *
 * NO incluye el modo de navegacion: ese ajuste es nativo de Ajustes
 * (Ajustes -> Sistema -> Gestos) y se deja donde estaba.
 */
public class OptimizationsActivity extends Activity {

    private static final String[] ANIMATION_SCALES = {"0x", "0.5x", "1x", "2x", "5x", "10x"};

    // Funcion USB. Se cambia escribiendo sys.usb.config: el init del device
    // tree (init.universal3475.usb.rc) ya tiene los handlers de mtp, mtp,adb,
    // ptp, ptp,adb, adb y none, y escribe en /sys/class/android_usb/android0.
    private static final String[] USB_FUNCTIONS = {"mtp,adb", "ptp,adb", "adb", "none"};
    private static final String[] USB_LABELS = {
            "Transferencia de archivos (MTP)", "Fotos (PTP)", "Solo ADB", "Solo carga"};

    private static final String USB_CHANNEL_ID = "j2lte-usb";
    private static final int USB_NOTIFICATION_ID = 4242;
    private static final String ACTION_USB_SET_FUNCTION =
            "com.android.settings.j2lte.SET_USB_FUNCTION";
    private static final String EXTRA_USB_FUNCTION = "usb_function";
    private static final String EXTRA_USB_LABEL = "usb_label";

    private ContentResolver mResolver;
    private ActivityManager mActivityManager;
    private TextView mMemoryInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResolver = getContentResolver();
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        root.addView(buildExtremeSwitch());
        root.addView(buildAnimationSpinner());
        root.addView(buildFinishActivitiesSwitch());
        root.addView(buildUsbFunctions());
        root.addView(buildMemoryInfo());
        root.addView(buildTrimCachesButton());

        setContentView(root);
        updateMemoryInfo();
    }

    private View buildExtremeSwitch() {
        Switch sw = new Switch(this);
        sw.setText(R.string.opt_extreme);
        sw.setSummary(R.string.opt_extreme_summary);
        sw.setChecked(isExtreme());
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applyExtreme(isChecked);
                // Refresca el resto de interruptores con el estado nuevo.
                recreate();
            }
        });
        return sw;
    }

    private View buildFinishActivitiesSwitch() {
        Switch sw = new Switch(this);
        sw.setText(R.string.opt_finish_activities);
        sw.setSummary(R.string.opt_finish_activities_summary);
        sw.setChecked(Settings.Global.getInt(mResolver,
                Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0) == 1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.Global.putInt(mResolver,
                        Settings.Global.ALWAYS_FINISH_ACTIVITIES, isChecked ? 1 : 0);
            }
        });
        return sw;
    }

    private View buildAnimationSpinner() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(this);
        label.setText(R.string.opt_animations);
        box.addView(label);

        TextView summary = new TextView(this);
        summary.setText(R.string.opt_animations_summary);
        box.addView(summary);

        Spinner spinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                ANIMATION_SCALES);
        spinner.setAdapter(spinner);
        spinner.setSelection(currentScaleIndex());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyScale(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        box.addView(spinner);
        return box;
    }

    private static float scaleForPosition(int position) {
        switch (position) {
            case 0: return 0f;
            case 1: return 0.5f;
            case 2: return 1f;
            case 3: return 2f;
            case 4: return 5f;
            default: return 10f;
        }
    }

    private int currentScaleIndex() {
        float scale = Settings.Global.getFloat(mResolver,
                Settings.Global.WINDOW_ANIMATION_SCALE, 1f);
        for (int i = 0; i < ANIMATION_SCALES.length; i++) {
            if (Math.abs(scaleForPosition(i) - scale) < 0.01f) return i;
        }
        return 2;
    }

    private void applyScale(int position) {
        float scale = scaleForPosition(position);
        Settings.Global.putFloat(mResolver, Settings.Global.WINDOW_ANIMATION_SCALE, scale);
        Settings.Global.putFloat(mResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, scale);
        Settings.Global.putFloat(mResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale);
    }

    private boolean isExtreme() {
        float scale = Settings.Global.getFloat(mResolver,
                Settings.Global.WINDOW_ANIMATION_SCALE, 1f);
        boolean finish = Settings.Global.getInt(mResolver,
                Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0) == 1;
        return scale == 0f && finish;
    }

    private void applyExtreme(boolean enabled) {
        applyScale(enabled ? 0 : 2);
        Settings.Global.putInt(mResolver, Settings.Global.ALWAYS_FINISH_ACTIVITIES,
                enabled ? 1 : 0);
    }

    private View buildMemoryInfo() {
        mMemoryInfo = new TextView(this);
        mMemoryInfo.setPadding(0, 16, 0, 0);
        return mMemoryInfo;
    }

    private void updateMemoryInfo() {
        if (mMemoryInfo == null) return;
        StringBuilder sb = new StringBuilder();
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            mActivityManager.getMemoryInfo(mi);
            long total = mi.totalMem / (1024 * 1024);
            long avail = mi.availMem / (1024 * 1024);
            long used = (mi.totalMem - mi.availMem) / (1024 * 1024);
            sb.append("RAM: ").append(used).append(" / ").append(total).append(" MB  (libre ")
              .append(avail).append(" MB)");
        } catch (Exception e) {
            sb.append("RAM: no disponible");
        }
        try {
            java.io.BufferedReader r = new java.io.BufferedReader(
                    new java.io.FileReader("/proc/swaps"));
            String line;
            long swapTotal = 0;
            long swapUsed = 0;
            while ((line = r.readLine()) != null) {
                String[] p = line.trim().split("\\s+");
                if (p.length >= 4 && !line.startsWith("Filename")) {
                    try {
                        swapTotal += Long.parseLong(p[1]) * 1024;
                        swapUsed += Long.parseLong(p[2]) * 1024;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            r.close();
            sb.append("\nSwap: ").append(swapUsed / (1024 * 1024)).append(" / ")
              .append(swapTotal / (1024 * 1024)).append(" MB");
        } catch (Exception e) {
        }
        mMemoryInfo.setText(sb.toString());
    }

    private View buildUsbFunctions() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(this);
        label.setText(R.string.opt_usb_function);
        box.addView(label);

        Spinner spinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, USB_LABELS);
        spinner.setAdapter(spinner);
        box.addView(spinner);

        Button apply = new Button(this);
        apply.setText(R.string.opt_usb_apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyUsbFunction(USB_FUNCTIONS[spinner.getSelectedItemPosition()]);
            }
        });
        box.addView(apply);
        return box;
    }

    private void applyUsbFunction(String function) {
        try {
            // El init reacciona a sys.usb.config y reconfigura el gadget.
            Runtime.getRuntime().exec("setprop sys.usb.config " + function);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "No se pudo cambiar la funcion USB",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Detecta la conexion USB y publica la notificacion con las funciones
     * disponibles. Es el equivalente de lo que hacia UsbProfileManager en
     * A11/A12, que A13 ya no trae: el SystemUI de A13 no tiene ningun
     * componente de perfil USB, y la pantalla de ajustes tampoco lo ofrece.
     */
    public static class UsbStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connected = intent.getBooleanExtra(UsbManager.EXTRA_CONNECTED, false);
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            if (!connected) {
                nm.cancel(USB_NOTIFICATION_ID);
                return;
            }
            showUsbNotification(context, nm);
        }
    }

    /** Aplica la funcion elegida desde la notificacion. */
    public static class UsbActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String function = intent.getStringExtra(EXTRA_USB_FUNCTION);
            if (function == null) return;
            try {
                Runtime.getRuntime().exec("setprop sys.usb.config " + function);
            } catch (Exception e) {
            }
        }
    }

    private static void showUsbNotification(Context context, NotificationManager nm) {
        try {
            if (nm.getNotificationChannel(USB_CHANNEL_ID) == null) {
                nm.createNotificationChannel(new NotificationChannel(USB_CHANNEL_ID,
                        context.getString(R.string.opt_usb_notif_channel),
                        NotificationManager.IMPORTANCE_DEFAULT));
            }
            Notification.Builder b = new Notification.Builder(context, USB_CHANNEL_ID);
            b.setContentTitle(context.getString(R.string.opt_usb_notif_title));
            b.setContentText(context.getString(R.string.opt_usb_notif_text));
            b.setSmallIcon(android.R.drawable.sym_def_app_icon);
            b.setOngoing(true);
            for (int i = 0; i < USB_FUNCTIONS.length; i++) {
                Intent intent = new Intent(ACTION_USB_SET_FUNCTION);
                intent.setPackage(context.getPackageName());
                intent.putExtra(EXTRA_USB_FUNCTION, USB_FUNCTIONS[i]);
                PendingIntent pi = PendingIntent.getBroadcast(context, i, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                b.addAction(new Notification.Action.Builder(null, USB_LABELS[i], pi).build());
            }
            nm.notify(USB_NOTIFICATION_ID, b.build());
        } catch (Exception e) {
        }
    }

    private View buildTrimCachesButton() {
        Button b = new Button(this);
        b.setText(R.string.opt_trim_caches);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Runtime.getRuntime().exec("cmd package trim-caches 0");
                } catch (Exception e) {
                    android.widget.Toast.makeText(OptimizationsActivity.this,
                            "No se pudo limpiar la cache", android.widget.Toast.LENGTH_SHORT).show();
                }
                updateMemoryInfo();
            }
        });
        return b;
    }
}
