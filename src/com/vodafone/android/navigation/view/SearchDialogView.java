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
package com.vodafone.android.navigation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.listeners.SearchProvidersUpdateListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.vodafone.android.navigation.util.ImageDownloader.ImageDownloadListener;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchQuery;

public class SearchDialogView extends LinearLayout implements ImageDownloadListener, SearchProvidersUpdateListener {

	private TextView searchWhat;
	private TextView searchLocation;
	private TextView searchProvider;
	private SearchQuery iSearchQuery;
    private ImageView searchProviderImage;
    private SearchProvider[] providers;
	
	public SearchDialogView(Context context, SearchQuery query) {
		super(context);
		this.iSearchQuery = query;
		init(context);
	}

	private void init(Context context) {
		View.inflate(context, R.layout.search_dialog, this);
		this.searchWhat = (TextView) findViewById(R.id.search_dialog_what);
		this.searchLocation = (TextView) findViewById(R.id.search_dialog_location);
        this.searchProviderImage = (ImageView) findViewById(R.id.search_provider_image);
        this.searchProvider = (TextView) findViewById(R.id.search_provider_name);
		
		if(this.iSearchQuery != null) {
    		Category category = iSearchQuery.getCategory();
            String itemQueryStr = iSearchQuery.getItemQueryStr();
            if(iSearchQuery.getQueryType() == SearchQuery.SEARCH_TYPE_REGIONAL){
    			if(category != null){
    	            String categoryName = category.getCategoryName();
                    String divider = ", ";
                    if(itemQueryStr == null || itemQueryStr.length() == 0) {
                        divider = "";
                    }
    				searchWhat.setText(itemQueryStr + divider + categoryName);
    			} else {
    				searchWhat.setText(itemQueryStr);
    			}
    			String searchAreaStr = iSearchQuery.getSearchAreaStr();
                String regionName = iSearchQuery.getTopRegion().getRegionName();
                String divider = ", ";
                if(searchAreaStr == null || searchAreaStr.length() == 0) {
                    divider = "";
                }
                searchLocation.setText(searchAreaStr + divider + regionName);
    		} else if(iSearchQuery.getQueryType() == SearchQuery.SEARCH_TYPE_POSITIONAL){
    			if(category != null){
    	            String categoryName = category.getCategoryName();
    			    String divider = ", ";
    			    if(itemQueryStr == null || itemQueryStr.length() == 0) {
    			        divider = "";
    			    }
    				searchWhat.setText(itemQueryStr + divider + categoryName);
    			} else {
    				searchWhat.setText(itemQueryStr);
    			}
    			searchLocation.setVisibility(View.GONE);
    		}

    		NavigatorApplication app = (NavigatorApplication) this.getContext().getApplicationContext();
    		
    		this.providers = app.getSearchProviders();
    		if(this.providers != null && this.providers.length > 0) {
    		    this.updateProvider();
    		}
    		else {
    		    app.setSearchProvidersUpdateListener(this);
    		}
		}
	}

    public void onImageDownloaded(final Bitmap scaledBitmap, final Bitmap origBitmap, String imageName, AndroidMapObject[] mapObjects) {
        Handler handler = this.getHandler();
        if(handler != null) {
            handler.post(new Runnable() {
                public void run() {
                    searchProviderImage.setImageBitmap(origBitmap);
                }
            });
        }
    }

    public void updateSearchProviders(SearchProvider[] providers) {
        this.providers = providers;
        this.updateProvider();
    }

    private void updateProvider() {
        Handler handler = this.getHandler();
        if(handler != null) {
            handler.post(new Runnable() {
                public void run() {
                    //TODO: this will display the first provider. We need to cycle thru them, or display all at the same time.
                    if(providers != null && providers.length > 0) {
                        SearchProvider p = providers[0];
                        searchProvider.setText(p.getProviderName());
                        if(p.getProviderImageName() != null && !"".equals(p.getProviderImageName())){
	                        Bitmap bitmap = ImageDownloader.get().queueDownload(SearchDialogView.this.getContext(), p.getProviderImageName(), null, SearchDialogView.this);
	                        if(bitmap != null) {
	                            searchProviderImage.setImageBitmap(bitmap);
	                        }
                        }
                    }
                }
            });
        }
    }
}
