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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;

public class SettingsActivity extends AbstractActivity implements OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.settings_activity);
        this.setTitle(R.string.qtn_andr_settings_tk);
        
        Resources res = this.getResources();
        
        ArrayList<String> content = new ArrayList<String>();
        content.add(res.getString(R.string.qtn_andr_gen_settings_tk));
        content.add(res.getString(R.string.qtn_andr_route_settings_tk));
//        content.add(res.getString(R.string.qtn_andr_map_settings_tk));
        if(NavigatorApplication.DEBUG_ENABLED) {
            content.add("Debug settings");
        }
        ListAdapter adapter = new SettingsArrayAdapter(this, content);
        
        ListView list = (ListView) this.findViewById(R.id.list_settings);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        switch(position) {
            case 0: {
                this.startActivity(new Intent(this, SettingsGeneralActivity.class));
                return;
            }
            case 1: {
                this.startActivity(new Intent(this, SettingsRouteActivity.class));
                return;
            }
//            case 2: {
//                this.startActivity(new Intent(this, SettingsMapActivity.class));
//                return;
//            }
            case 2: {
                this.startActivity(new Intent(this, SettingsDebugActivity.class));
                return;
            }
        }
    }
    
    private static class SettingsArrayAdapter extends ArrayAdapter<String> {
        private Activity context;

        public SettingsArrayAdapter(Activity context, ArrayList<String> content) {
            super(context, R.layout.settings_list_item, content);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = View.inflate(context, R.layout.settings_list_item, null);
            TextView labelView = (TextView) row.findViewById(R.id.setting_name);
            String title = this.getItem(position);
            if(title != null){
                labelView.setText(title);
            }
            
            return row;
        }
    }
}
