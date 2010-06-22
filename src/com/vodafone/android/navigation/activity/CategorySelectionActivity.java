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

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.CategoriesAdapter;
import com.vodafone.android.navigation.listeners.SearchCategoriesListUpdatedListener;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryCollection;

public class CategorySelectionActivity extends AbstractActivity implements SearchCategoriesListUpdatedListener {

	private ListView categoriesListView;
	private CategoriesAdapter categoryAdapter;
	private ArrayList<Category> categories;
	private NavigatorApplication application;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_selection_activity);
        
        this.application = (NavigatorApplication) this.getApplicationContext();
        this.application.addSearchCategoriesListUpdatedListener(this);
        
        categoriesListView = (ListView) findViewById(R.id.listview_categories);
        updateUIArrays(this.application.getCategoryCollection());
        categoryAdapter = new CategoriesAdapter(this, categories);
		categoriesListView.setAdapter(categoryAdapter);
		
		categoriesListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Category cat = null;
				
				try{
					cat = categories.get(position);
				} catch(ArrayIndexOutOfBoundsException e){
				}
				application.setSelectedSearchCategory(cat);
				finish();
			}
		});
    }

	private void updateUIArrays(CategoryCollection aCategoryCollection) {
		if(categories == null) {
			categories = new ArrayList<Category>();
		} else {
			categories.clear();
		}
		categories.add(null);

		if(aCategoryCollection != null) {
		    for(int i = 0; i < aCategoryCollection.getNbrOfCategories(); i++){
		        categories.add(aCategoryCollection.getCategory(i));
		    }
		}
	}

	public void onSearchCategoriesListUpdated(CategoryCollection catCollection) {
		updateUIArrays(catCollection);
		categoryAdapter.notifyDataSetChanged();
		
	}

	@Override
	protected void onDestroy() {
		this.application.removeSearchCategoriesListUpdatedListener(this);
		super.onDestroy();
	}
}
