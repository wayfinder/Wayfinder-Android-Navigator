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
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.view.SettingsCheckBox;
import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.shared.route.RouteSettings;

public class SettingsRouteActivity extends AbstractActivity {

    private ApplicationSettings appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.settings_route_activity);
        this.setTitle(R.string.qtn_andr_route_settings_tk);

        this.appSettings = ApplicationSettings.get();

        this.setupRouteOptimization();
        this.setupVoiceGuidance();
        this.setupRouteOptions();
        this.setupPoiCategories();
    }

    private void setupPoiCategories() {
        LinearLayout layoutCategories = (LinearLayout) this.findViewById(R.id.layout_categories);
        
        PoiCategory[] poiCats = this.getApp().getMapInterface().getMapDetailedConfigInterface().getPoiCategories();
        this.appSettings.setupPoiCategories(poiCats);
        for(final PoiCategory poiCat: poiCats) {
            SettingsCheckBox checkbox = new SettingsCheckBox(this);
            checkbox.setText(poiCat.getName());
            checkbox.setChecked(poiCat.isEnable());
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    appSettings.setPoiCategoryEnabled(poiCat, isChecked);
                }
            });
            layoutCategories.addView(checkbox, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        layoutCategories.invalidate();
        layoutCategories.requestLayout();
    }

    private void setupRouteOptimization() {
        int rotueOpt = this.appSettings.getRouteOptimization();
        switch(rotueOpt) {
            case RouteSettings.OPTIMIZE_DISTANCE: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_route_distance);
                button.setChecked(true);
                break;
            }
            case RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC: {
                RadioButton button = (RadioButton) this.findViewById(R.id.radio_route_time);
                button.setChecked(true);
                break;
            }
        }

        RadioGroup rg = (RadioGroup) this.findViewById(R.id.radio_group_route_optimization);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup rg, int position) {
                int id = rg.getCheckedRadioButtonId();
                switch(id) {
                    case R.id.radio_route_distance: {
                        appSettings.setRouteOptimization(RouteSettings.OPTIMIZE_DISTANCE);
                        break;
                    }
                    case R.id.radio_route_time: {
                        appSettings.setRouteOptimization(RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC);
                        break;
                    }
                    default: {
                        appSettings.setRouteOptimization(RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC);
                    }
                }
            }
        });
    }

    private void setupVoiceGuidance() {
        boolean voiceGuide = this.appSettings.getRouteUseVoiceGuidance();
        CheckBox box = (CheckBox) this.findViewById(R.id.check_box_voice);
        box.setChecked(voiceGuide);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appSettings.setRouteUseVoiceGuidance(isChecked);
            }
        });
    }

    private void setupRouteOptions() {
        boolean tollRoads = this.appSettings.getRouteUseTollRoads();
        CheckBox box = (CheckBox) this.findViewById(R.id.check_box_toll_roads);
        box.setChecked(tollRoads);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appSettings.setRouteUseTollRoads(isChecked);
            }
        });
        
        boolean motorway = this.appSettings.getRouteUseHighways();
        box = (CheckBox) this.findViewById(R.id.check_box_highway);
        box.setChecked(motorway);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appSettings.setRouteUseHighways(isChecked);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.appSettings.commit();
    }
}
