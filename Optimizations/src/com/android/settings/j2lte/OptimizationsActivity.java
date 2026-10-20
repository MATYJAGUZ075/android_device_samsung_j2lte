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
import android.content.ContentResolver;
import android.os.Bundle;
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

    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResolver = getContentResolver();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        root.addView(buildExtremeSwitch());
        root.addView(buildAnimationSpinner());
        root.addView(buildFinishActivitiesSwitch());

        setContentView(root);
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
}
