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
import java.util.Comparator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.SearchHistoryAdapter;
import com.vodafone.android.navigation.components.SearchHistoryItem;

public class PreviousSearchesActivity extends AbstractActivity  {
	
	private ListView searchHistorylistView;
	private SearchHistoryAdapter searchHistoryAdapter;
	private ArrayList<SearchHistoryItem> searchHistory;
	
	private static final int MENU_ITEM_CLEAR_LIST = 0;
	public static final String DISABLED_STR = "@disabled:";
	private MenuItem cmdClear;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_items_list_activity);
        
        final NavigatorApplication app = this.getApp();

        searchHistorylistView = (ListView) findViewById(R.id.listview);
        searchHistory = getApp().getPreviousSearches();
        setupSearchHistoryList();
        searchHistoryAdapter = new SearchHistoryAdapter(this, searchHistory);
        searchHistorylistView.setAdapter(searchHistoryAdapter);
		
        searchHistorylistView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				SearchHistoryItem selectedItem = searchHistoryAdapter.getItem(position);
				app.setPreviousSearchQuery(selectedItem);
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
        searchHistoryAdapter.sort(new Comparator<SearchHistoryItem>(){
			public int compare(SearchHistoryItem item1, SearchHistoryItem item2) {
				return (int)(item2.getTimestamp() - item1.getTimestamp());
			}
		});
    }

	private void setupSearchHistoryList() {
		NavigatorApplication app = getApp();
		for(SearchHistoryItem item: searchHistory){
			item.setupInternalData(app.getTopRegionCollection(), app.getCategoryCollection());
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case MENU_ITEM_CLEAR_LIST:
			getApp().removeAllPreviousSearches();
			searchHistory = getApp().getPreviousSearches();
			handleEmptyList();
			searchHistoryAdapter.notifyDataSetChanged();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void handleEmptyList() {
		if(searchHistory.isEmpty()){
			cmdClear.setEnabled(false);
			searchHistory = new ArrayList<SearchHistoryItem>();
			searchHistory.add(new SearchHistoryItem(0, DISABLED_STR + this.getString(R.string.qtn_andr_no_prev_searches_txt), null, 0, 0, 0));
			searchHistoryAdapter = new SearchHistoryAdapter(this, searchHistory);
	        searchHistorylistView.setAdapter(searchHistoryAdapter);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		cmdClear = menu.add(Menu.NONE, MENU_ITEM_CLEAR_LIST, Menu.NONE, R.string.qtn_andr_clear_tk);
		cmdClear.setIcon(R.drawable.cmd_remove);
		cmdClear.setEnabled(!getApp().getPreviousSearches().isEmpty());
		return super.onCreateOptionsMenu(menu);
	}
}
