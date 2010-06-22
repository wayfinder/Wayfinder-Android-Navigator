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
import com.vodafone.android.navigation.activity.PreviousSearchesActivity;
import com.vodafone.android.navigation.components.SearchHistoryItem;

public class SearchHistoryAdapter extends ArrayAdapter<SearchHistoryItem> {
	private Activity context;
	
	public SearchHistoryAdapter(Activity aContext, ArrayList<SearchHistoryItem> searchHistory) {
		super(aContext, R.layout.search_history_list_item, 	searchHistory);
		context = aContext;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		SearchHistoryItem item = getItem(position);
		
		if(item.getWhatText().startsWith(PreviousSearchesActivity.DISABLED_STR)){
			View row = inflater.inflate(R.layout.empty_list_item, null);
			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(item.getWhatText().replace(PreviousSearchesActivity.DISABLED_STR, ""));
			row.setEnabled(false);
			return row;
		}
		
		View row = convertView;
		ViewWrapper vw;
		if(row == null) {
		    row = inflater.inflate(R.layout.search_history_list_item, null);
		    vw = new ViewWrapper(row);
		    row.setTag(vw);
		}
		else {
		    vw = (ViewWrapper) row.getTag();
		}
		TextView what = vw.getWhat();
		TextView where = vw.getWhere();
		what.setText(item.getWhatText());
		StringBuffer sb = new StringBuffer();
		if(item.getCategory() != null){
			sb.append(item.getCategory().getCategoryName());
			sb.append(", ");
		}
		if(item.getWhereText().trim().length() > 0){
			sb.append(item.getWhereText());
			sb.append(", ");
		}
		if(item.getTopRegion() != null){
			sb.append(item.getTopRegion().getRegionName());
		}
		where.setText(sb.toString());
		
		return row;
	}
	
	private static class ViewWrapper {

        private View view;
        private TextView what;
        private TextView where;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public TextView getWhere() {
            if(this.where == null) {
                this.where = (TextView) this.view.findViewById(R.id.search_where);
            }
            return this.where;
        }

        public TextView getWhat() {
            if(this.what == null) {
                this.what = (TextView) this.view.findViewById(R.id.search_what);
            }
            return this.what;
        }
	}
}
