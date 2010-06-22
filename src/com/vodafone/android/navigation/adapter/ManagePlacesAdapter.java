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
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.PlacesManagerItem;
import com.vodafone.android.navigation.util.ImageDownloader;


public class ManagePlacesAdapter extends ArrayAdapter<PlacesManagerItem> {
	private Activity context;
	
	
	
	public ManagePlacesAdapter(Activity aContext, ArrayList<PlacesManagerItem> places) {
		super(aContext, R.layout.manage_places_list_item,
				places);
		context = aContext;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View row = convertView;
	    ViewWrapper vw;
	    if(row == null) {
    		LayoutInflater inflater = context.getLayoutInflater();
    		row = inflater.inflate(R.layout.manage_places_list_item, null);
    		vw = new ViewWrapper(row);
    		row.setTag(vw);
	    }
	    else {
	        vw = (ViewWrapper) row.getTag();
	    }
		TextView name = vw.getName();
		TextView description = vw.getDesc();
		PlacesManagerItem item = getItem(position);
		name.setText(item.getItemName());
		description.setText(item.getItemDescription());
		ImageView image = vw.getImage();
		CheckBox box = vw.getBox();
		box.setChecked(item.isChecked());
		box.setClickable(false);
		box.setFocusable(false);
		box.setFocusableInTouchMode(false);
        Bitmap bitmap = ImageDownloader.get().getImage(this.context, item.getItemImageName(), true);
        if(bitmap != null) {
            image.setImageBitmap(bitmap);
        }
		
		return row;
	}
	
	private static class ViewWrapper {

        private View view;
        private TextView name;
        private TextView desc;
        private ImageView image;
        private CheckBox box;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public CheckBox getBox() {
            if(this.box == null) {
                this.box = (CheckBox) this.view.findViewById(R.id.check_box);
            }
            return this.box;
        }

        public ImageView getImage() {
            if(this.image == null) {
                this.image = (ImageView) this.view.findViewById(R.id.category_icon);
            }
            return this.image;
        }

        public TextView getDesc() {
            if(this.desc == null) {
                this.desc = (TextView) this.view.findViewById(R.id.text_description);
            }
            return this.desc;
        }

        public TextView getName() {
            if(this.name == null) {
                this.name = (TextView) this.view.findViewById(R.id.text_title);
            }
            return this.name;
        }
	    
	}
}
