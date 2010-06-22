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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.vodafone.android.navigation.util.ImageDownloader.ImageDownloadListener;
import com.wayfinder.core.search.Category;

public class CategoriesAdapter extends ArrayAdapter<Category> implements ImageDownloadListener {

    private Activity context;
    private HashMap<String, ImageView> map = new HashMap<String, ImageView>();
    private Handler handler = new Handler();
	
	public CategoriesAdapter(Activity context, ArrayList<Category> aCategories) {
		super(context, R.layout.category_list_item, aCategories);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    NavigatorApplication app = (NavigatorApplication) this.context.getApplication();
		LayoutInflater inflater = context.getLayoutInflater();
		View row = convertView;
		ViewWrapper vw;
		if(row == null) {
		    row = inflater.inflate(R.layout.category_list_item, null);
		    vw = new ViewWrapper(row);
		    row.setTag(vw);
		}
		else {
		    vw = (ViewWrapper) row.getTag();
		}
		
		LinearLayout layout = vw.getLayout();
		TextView labelView = vw.getLabel();
		ImageView imageView = vw.getIcon();
		Category cat = getItem(position);
		if(cat != null){
			labelView.setText(cat.getCategoryName());
            Bitmap bitmap = ImageDownloader.get().getImage(this.context, cat.getCategoryImageName(), true);
			if(bitmap != null) {
			    imageView.setImageBitmap(bitmap);
			}
			else {
				imageView.setImageResource(R.drawable.cat_all);
	            ImageDownloader.get().queueDownload(this.context, cat.getCategoryImageName(), null, this);
			    this.map.put(cat.getCategoryImageName(), imageView);
			}
			
            Category selCat = app.getSelectedSearchCategory();
            if(cat.equals(selCat)) {
                 layout.setBackgroundResource(R.color.color_blue_dark);
            } else {
            	 layout.setBackgroundResource(R.color.color_white);
            }
		} else {
			labelView.setText(context.getString(R.string.qtn_andr_no_categories_txt));
			imageView.setImageResource(R.drawable.cat_all);
		}
		
		return row;
	}

    public void onImageDownloaded(final Bitmap scaledBitmap, final Bitmap origBitmap, final String imageName, AndroidMapObject[] mapObjects) {
        if(this.map.containsKey(imageName)) {
            final ImageView imageView = this.map.get(imageName);
            if(imageView != null) {
                this.handler.post(new Runnable() {
                    public void run() {
                        imageView.setImageBitmap(origBitmap);
                        map.remove(imageName);
                    }
                });
            }
        }
    }
    
    private static class ViewWrapper {
        private View view;
        private LinearLayout layout;
        private TextView label;
        private ImageView icon;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public ImageView getIcon() {
            if(this.icon == null) {
                this.icon = (ImageView) this.view.findViewById(R.id.category_icon);
            }
            return this.icon;
        }

        public TextView getLabel() {
            if(this.label == null) {
                this.label = (TextView) this.view.findViewById(R.id.category_name);
            }
            return this.label;
        }

        public LinearLayout getLayout() {
            if(this.layout == null) {
                this.layout = (LinearLayout) this.view.findViewById(R.id.layout);
            }
            return this.layout;
        }
        
    }
}
