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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Utilidad USB del J2.
 *
 * A13 ya no trae el gestor de perfiles USB que habia en A11/A12 (lo sustituyo
 * por UsbDeviceManager, que no ofrece ninguna pantalla de perfil), asi que el
 * gadget USB se queda en "ADB only" salvo que se le cambie la funcion a mano.
 *
 * Esta app hace dos cosas:
 *   - pantalla para elegir la funcion USB (MTP, PTP, solo ADB, solo carga);
 *   - notificacion al conectar el cable, con un boton por funcion.
 *
 * El panel de "Optimizaciones" NO esta aqui: vive dentro de la propia app de
 * Ajustes (Ajustes -> Sistema -> Optimizaciones).
 */
public class UsbFunctionActivity extends Activity {

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

    private static final int REQ_POST_NOTIFICATIONS = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // A13 exige POST_NOTIFICATIONS para publicar la notificacion de "USB
        // conectado". Es un permiso en tiempo de ejecucion: se pide la primera
        // vez que se abre la app.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIFICATIONS);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        root.addView(buildUsbFunctions());

        setContentView(root);
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
        if (!setUsbConfig(function)) {
            Toast.makeText(this, R.string.opt_usb_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Escribe sys.usb.config. El init reacciona a esa propiedad y reconfigura
     * el gadget (los handlers estan en init.universal3475.usb.rc).
     */
    private static boolean setUsbConfig(String function) {
        try {
            Runtime.getRuntime().exec("setprop sys.usb.config " + function);
            return true;
        } catch (Exception e) {
            return false;
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
            setUsbConfig(function);
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
}
