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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.view.SettingsCheckBox;
import com.wayfinder.core.Core;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.shared.Position;

public class SettingsDebugActivity extends AbstractActivity {

    private ApplicationSettings appSettings;
    private TextView textServerUrl;
    private TextView textServerPort;
    private TextView textClientType;
    private SettingsCheckBox checkboxAlwaysRoute;
    private SettingsCheckBox checkboxDispSettingsAtStartup;
    private SettingsCheckBox checkboxUseFixedZoom;
    private SettingsCheckBox checkboxDisablePositioning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTitle("Debug settings");
        this.setContentView(R.layout.settings_debug_activity);

        this.textServerUrl = (TextView) this.findViewById(R.id.text_server_url);
        this.textServerPort = (TextView) this.findViewById(R.id.text_server_port);
        this.textClientType = (TextView) this.findViewById(R.id.text_client_type);
        this.checkboxAlwaysRoute = (SettingsCheckBox) this.findViewById(R.id.check_box_always_route);
        this.checkboxDispSettingsAtStartup = (SettingsCheckBox) this.findViewById(R.id.check_box_disp_settings_at_startup);
        this.checkboxUseFixedZoom = (SettingsCheckBox) this.findViewById(R.id.check_box_fixed_zoom);
        this.checkboxDisablePositioning = (SettingsCheckBox) this.findViewById(R.id.checkbox_disable_positioning);

        final TextView textCoordinateLat = (TextView) this.findViewById(R.id.text_coordinate_lat);
        final TextView textCoordinateLon = (TextView) this.findViewById(R.id.text_coordinate_lon);
        NavigatorApplication app = getApp();
        if(app.isInitialized()) {
            Core core = app.getCore();
            Position pos = core.getVectorMapInterface().getActivePosition();
            textCoordinateLat.setText("" + pos.getMc2Latitude());
            textCoordinateLon.setText("" + pos.getMc2Longitude());
        }
        Button btnShowCoordinateInMap = (Button) this.findViewById(R.id.btn_set_coordinate);
        btnShowCoordinateInMap.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NavigatorApplication app = getApp();
                if(app.isInitialized()) {
                    checkboxDisablePositioning.setChecked(true);
                    appSettings.setDisablePositioning(true);
                    appSettings.commit();

                    Core core = app.getCore();
                    VectorMapInterface map = core.getVectorMapInterface();
                    int mc2Lat = Integer.parseInt(textCoordinateLat.getText().toString());
                    int mc2Lon = Integer.parseInt(textCoordinateLon.getText().toString());
                    app.setOwnLocationInformation(mc2Lat, mc2Lon, 10);

                    map.setFollowGpsPosition(false);
                    map.setGpsPosition(mc2Lat, mc2Lon, 0);
                    map.setCenter(mc2Lat, mc2Lon);
                    map.requestMapUpdate();
                    
                    Toast.makeText(SettingsDebugActivity.this, "New mapcoordinate is: [" + mc2Lat + ", " + mc2Lon + "]", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnProdServer = (Button) this.findViewById(R.id.btn_prod_server);
        btnProdServer.setOnClickListener(new OnClickListener() {
            public void onClick(View button) {
                textServerUrl.setText("xml.prodserver.url.com");
                textServerPort.setText("80");
                textClientType.setText("[CLIENTTYPE]");
            }
        });

        Button btnDefTestServer = (Button) this.findViewById(R.id.btn_test_default_server);
        btnDefTestServer.setOnClickListener(new OnClickListener() {
            public void onClick(View button) {
                textServerUrl.setText("xml.testserver.url.com");
                textServerPort.setText("12211");
                textClientType.setText("[CLIENTTYPE]");
            }
        });

        this.appSettings = ApplicationSettings.get();

        this.setupServer();
    }
    
    @Override
    protected boolean canInitiateCore() {
        return false;
    }

    private void setupServer() {
        String serverUrl = this.appSettings.getServerUrl();
        int serverPort = this.appSettings.getServerPort();
        String clientType = this.appSettings.getClientId();
        boolean alwaysRoute = this.appSettings.getAlwaysRoute();
        boolean dispSettingsAtStartup = this.appSettings.getDisplaySettingsAtStartup();
        boolean useFixedZoomLevels = this.appSettings.getUseFixedZoomLevels();
        boolean disablePositioning = this.appSettings.getDisablePositioning();
        
        this.textServerUrl.setText(serverUrl);
        this.textServerPort.setText("" + serverPort);
        this.textClientType.setText(clientType);
        this.checkboxAlwaysRoute.setChecked(alwaysRoute);
        this.checkboxDispSettingsAtStartup.setChecked(dispSettingsAtStartup);
        this.checkboxUseFixedZoom.setChecked(useFixedZoomLevels);
        this.checkboxDisablePositioning.setChecked(disablePositioning);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        //TODO: check if server-address and/or port has changed. Then reinitiate core...
        
        String serverUrl = this.textServerUrl.getText().toString();
        int serverPort = Integer.parseInt(this.textServerPort.getText().toString());
        String clientType = this.textClientType.getText().toString();
        boolean alwaysRoute = this.checkboxAlwaysRoute.isChecked();
        boolean dispSettingsAtStartup = this.checkboxDispSettingsAtStartup.isChecked();
        boolean useFixedZoomLevels = this.checkboxUseFixedZoom.isChecked();
        boolean disablePositioning = this.checkboxDisablePositioning.isChecked();
        
        this.appSettings.setServerAddress(serverUrl, serverPort);
        this.appSettings.setClientId(clientType);
        this.appSettings.setAlwaysRoute(alwaysRoute);
        this.appSettings.setDisplaySettingsAtStartup(dispSettingsAtStartup);
        this.appSettings.setUseFixedZoomLevels(useFixedZoomLevels);
        this.appSettings.setDisablePositioning(disablePositioning);
        
        this.appSettings.commit();
    }
}
