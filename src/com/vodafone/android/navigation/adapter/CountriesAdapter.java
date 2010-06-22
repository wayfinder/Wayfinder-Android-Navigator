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
package com.vodafone.android.navigation.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vodafone.android.navigation.R;

public class CountriesAdapter extends ArrayAdapter<String> {
	private Activity context;
	
	public CountriesAdapter(Activity context, ArrayList<String> countriesList) {
		super(context, R.layout.country_list_item, countriesList);
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		String itemValue = getItem(position).replace("\t", "");
		return (itemValue.length() > 1);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View row = convertView;
        ViewWrapper vw;
	    if(row == null) {
    		LayoutInflater inflater = context.getLayoutInflater();
    		row = inflater.inflate(R.layout.country_list_item, null);
    		vw = new ViewWrapper(row);
    		row.setTag(vw);
	    }
	    else {
	        vw = (ViewWrapper) row.getTag();
	    }
		TextView separator = vw.getSeperator();
		TextView countryName = vw.getCountry();
		String itemValue = getItem(position).replace("\t", "");
		if(itemValue.length() > 1){
			countryName.setText(itemValue);
			separator.setVisibility(View.GONE);
			countryName.setVisibility(View.VISIBLE);
		} else {
			separator.setText(itemValue);
			separator.setVisibility(View.VISIBLE);
			countryName.setVisibility(View.GONE);
		}
		return row;
	}
	
	private static class ViewWrapper {

        private View view;
        private TextView seperator;
        private TextView country;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public TextView getCountry() {
            if(this.country == null) {
                this.country = (TextView) this.view.findViewById(R.id.country_name);
            }
            return this.country;
        }

        public TextView getSeperator() {
            if(this.seperator == null) {
                this.seperator = (TextView) this.view.findViewById(R.id.separator_label);
            }
            return this.seperator;
        }
	    
	}
}
