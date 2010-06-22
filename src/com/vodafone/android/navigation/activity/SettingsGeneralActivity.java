/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
package com.vodafone.android.navigation.activity;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.wayfinder.core.shared.settings.GeneralSettings;

public class SettingsGeneralActivity extends AbstractActivity {

    private ApplicationSettings appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.settings_general_activity);
        this.setTitle(R.string.qtn_andr_gen_settings_tk);

        this.appSettings = ApplicationSettings.get();

        this.setupBacklight();
        this.setupMeasurements();
    }

    private void setupBacklight() {
        int backlightSetting = this.appSettings.getBacklight();
        switch(backlightSetting) {
            case ApplicationSettings.BACKLIGHT_NORMAL: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_backlight_normal);
                button.setChecked(true);
                break;
            }
            case ApplicationSettings.BACKLIGHT_ON_ROUTE: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_backlight_on_route);
                button.setChecked(true);
                break;
            }
            case ApplicationSettings.BACKLIGHT_ALWAYS_ON: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_backlight_always_on);
                button.setChecked(true);
                break;
            }
        }

        RadioGroup rg = (RadioGroup) this.findViewById(R.id.radio_group_backlight);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup rg, int position) {
                int id = rg.getCheckedRadioButtonId();
                switch(id) {
                    case R.id.radio_backlight_normal: {
                        appSettings.setBacklight(ApplicationSettings.BACKLIGHT_NORMAL);
                        break;
                    }
                    case R.id.radio_backlight_on_route: {
                        appSettings.setBacklight(ApplicationSettings.BACKLIGHT_ON_ROUTE);
                        break;
                    }
                    case R.id.radio_backlight_always_on: {
                        appSettings.setBacklight(ApplicationSettings.BACKLIGHT_ALWAYS_ON);
                        break;
                    }
                    default: {
                        appSettings.setBacklight(ApplicationSettings.BACKLIGHT_ON_ROUTE);
                    }
                }
            }
        });
    }

    private void setupMeasurements() {
        int units = this.appSettings.getMeasurementSystem();
        switch(units) {
            case GeneralSettings.UNITS_METRIC: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_metric);
                button.setChecked(true);
                break;
            }
            case GeneralSettings.UNITS_IMPERIAL_US: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_miles_feet);
                button.setChecked(true);
                break;
            }
            case GeneralSettings.UNITS_IMPERIAL_UK: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_miles_yards);
                button.setChecked(true);
                break;
            }
        }

        RadioGroup rg = (RadioGroup) this.findViewById(R.id.radio_group_units);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup rg, int position) {
                int id = rg.getCheckedRadioButtonId();
                switch(id) {
                    case R.id.radio_metric: {
                        appSettings.setMeasurementSystem(GeneralSettings.UNITS_METRIC);
                        break;
                    }
                    case R.id.radio_miles_feet: {
                        appSettings.setMeasurementSystem(GeneralSettings.UNITS_IMPERIAL_US);
                        break;
                    }
                    case R.id.radio_miles_yards: {
                        appSettings.setMeasurementSystem(GeneralSettings.UNITS_IMPERIAL_UK);
                        break;
                    }
                    default: {
                        appSettings.setMeasurementSystem(GeneralSettings.UNITS_METRIC);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.appSettings.commit();
    }
}
