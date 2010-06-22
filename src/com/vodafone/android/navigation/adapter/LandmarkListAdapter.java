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
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.Landmark;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class LandmarkListAdapter extends ArrayAdapter<Landmark> implements OnClickListener {
	public static final String DISABLED_STR = "@disabled:";
	public static final String SEPARATOR_STR = "@separator:";
	
	private Activity context;
    private HashMap<View, Landmark> mappedViews = new HashMap<View, Landmark>();
    private OnSearchMatchClickListener listener;

	public LandmarkListAdapter(Activity context, ArrayList<Landmark> headings, OnSearchMatchClickListener listener) {
		super(context, R.layout.search_results_list_item, headings);
		this.context = context;
		this.listener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();

		Landmark item = getItem(position);
		if(item.getName().startsWith(DISABLED_STR)){
	        View row = inflater.inflate(R.layout.empty_list_item, null);
			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(item.getName().replace(DISABLED_STR, ""));
			row.setEnabled(false);
			return row;
		} else if(item.getName().startsWith(SEPARATOR_STR)){
			View row = inflater.inflate(R.layout.separator_list_item, null);
			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(item.getName().replace(SEPARATOR_STR, ""));
			row.setEnabled(false);
			return row;
		} 

		ViewWrapper viewWrapper;
		View row = convertView;
		if(row == null || row.getTag() == null) {
		    row = inflater.inflate(R.layout.search_results_list_item, null);
		    viewWrapper = new ViewWrapper(row);
		    row.setTag(viewWrapper);
		}
		else {
		    viewWrapper = (ViewWrapper) row.getTag();
		}
		
		TextView resultName = viewWrapper.getTitle();
		TextView resultLocation = viewWrapper.getDesc();
		TextView distance = viewWrapper.getDistance();
		
		NavigatorApplication app = (NavigatorApplication) this.context.getApplicationContext();
        LocationInformation loc = app.getOwnLocationInformation();
        if(loc != null) {
    		Position pos = loc.getMC2Position();
    		int distanceInMeters = pos.distanceTo(item.getPosition());
    		FormattingResult result = app.getUnitsFormatter().formatDistance(distanceInMeters);
    		String distanceText = result.getRoundedValue() + " " + result.getUnitAbbr();
    		distance.setText(distanceText);
        }
		
		resultName.setText(item.getName());

		String matchLocation = item.getDescription();
		if(matchLocation == null) {
			matchLocation = "";
		}
		resultLocation.setText(matchLocation);
		
        ImageView image = viewWrapper.getIcon();
		
        Bitmap bitmap = ImageDownloader.get().getImage(this.context, item.getImageName(), true);
        if(bitmap != null) {
            image.setImageBitmap(bitmap);
        }
		
		this.mappedViews.put(row, item);
		row.setOnClickListener(this);
		
		return row;
	}

    public void onClick(View view) {
        Landmark searchMatch = this.mappedViews.get(view);
        this.listener.onClick(view, searchMatch);
    }

    public interface OnSearchMatchClickListener {
        void onClick(View view, Landmark searchMatch);
    }
    
    private static class ViewWrapper {
        private View view;
        private TextView title;
        private TextView desc;
        private TextView dist;
        private ImageView icon;
        private ImageView favIcon;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public ImageView getFavIcon() {
            if(this.favIcon == null) {
                this.favIcon = (ImageView) this.view.findViewById(R.id.img_isFavorite);
            }
            return this.favIcon;
        }

        public ImageView getIcon() {
            if(this.icon == null) {
                this.icon = (ImageView) this.view.findViewById(R.id.category_icon);
            }
            return this.icon;
        }

        public TextView getDistance() {
            if(this.dist == null) {
                this.dist = (TextView) this.view.findViewById(R.id.text_distance);
            }
            return this.dist;
        }

        public TextView getDesc() {
            if(this.desc == null) {
                this.desc = (TextView) this.view.findViewById(R.id.text_description);
            }
            return this.desc;
        }

        public TextView getTitle() {
            if(this.title == null) {
                this.title = (TextView) this.view.findViewById(R.id.text_title);
            }
            return this.title;
        }
    }
}
