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
import java.util.Stack;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.CountriesAdapter;
import com.vodafone.android.navigation.listeners.TopRegionsListUpdatedListener;
import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.TopRegionCollection;

public class CountrySelectionActivity extends AbstractActivity implements TopRegionsListUpdatedListener, TextWatcher {

	private ListView countriesListView;
	private CountriesAdapter countriesAdapter;
	private ArrayList<String> countryNames;
	private EditText countryFilter;
	private NavigatorApplication application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.country_selection_activity);
        
        this.application = (NavigatorApplication) this.getApplicationContext();
        this.application.addTopRegionsListUpdatedListener(this);

        countryFilter = (EditText) findViewById(R.id.list_filter);
        countryFilter.addTextChangedListener(this);
        countriesListView = (ListView) findViewById(R.id.listview_countries);
        updateUIArrays(this.application.getTopRegionCollection());
        countriesAdapter = new CountriesAdapter(this, countryNames);
		countriesListView.setAdapter(countriesAdapter);
		
		countriesListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				application.setSelectedTopRegion(countriesAdapter.getItem(position).replace("\t", ""));
				finish();
			}
		});
    }

	private void updateUIArrays(TopRegionCollection aTopRegionCollection) {
		String separator = "";
		if(countryNames == null) {
			countryNames = new ArrayList<String>();
		} else {
			countryNames.clear();
		}
		
		Stack<TopRegion> stack = this.application.getLastUsedTopRegions();
		for(int i = 0; i < stack.size(); i++){
			countryNames.add("\t" + stack.get(i).getRegionName());
		}
		if(aTopRegionCollection != null) {
    		for(int i = 0; i < aTopRegionCollection.getNbrOfRegions(); i++){
    			final TopRegion topRegion = aTopRegionCollection.getTopRegion(i);
    			if(!("\t" + topRegion.getRegionName().substring(0, 1)).equalsIgnoreCase(separator)){
    				separator = "\t" + topRegion.getRegionName().substring(0, 1).toUpperCase();
    				countryNames.add(separator);
    			}
    			countryNames.add(topRegion.getRegionName());
    		}
		}
	}

	@Override
	protected void onDestroy() {
		this.application.removeTopRegionsListUpdatedListener(this);
		super.onDestroy();
	}

	public void onTopRegionsListUpdated(TopRegionCollection topRegions) {
		updateUIArrays(topRegions);
		countriesAdapter.notifyDataSetChanged();
	}

	public void afterTextChanged(Editable s) {
		countriesAdapter.getFilter().filter(countryFilter.getText());
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
