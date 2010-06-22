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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.ManagePlacesAdapter;
import com.vodafone.android.navigation.components.PlacesManagerItem;
import com.vodafone.android.navigation.components.RecentDestination;
import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.ListDataListener;
import com.wayfinder.core.favorite.ListModel;

public class ManagePlacesActivity extends AbstractActivity implements ListDataListener {
	
	private ListView listView;
	private ManagePlacesAdapter managePlacesAdapter;
	private ListModel places;
	private RecentDestination[] recentDestinations;
	private ArrayList<PlacesManagerItem> uiItems;
	private int type;
	
	
	private static final int MENU_ITEM_SELECT_ALL = 0;
	private static final int MENU_ITEM_REMOVE = 1;
	private static final int MENU_ITEM_UNSELECT_ALL = 2;
	
	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_items_list_activity);
		listView = (ListView) findViewById(R.id.listview);
		NavigatorApplication app = getApp();
        Intent intent = this.getIntent();
        this.type = intent.getIntExtra(PlacesActivity.KEY_TYPE, PlacesActivity.TYPE_SAVED_PLACES);
        uiItems = new ArrayList<PlacesManagerItem>();
        if(type == PlacesActivity.TYPE_SAVED_PLACES){
        	places = app.getSavedPlaces();	
        	updateUIArrays(places);
        	this.setTitle(R.string.qtn_andr_mng_my_places_txt);
        }else{
        	recentDestinations = app.getPreviousDestinations();
        	this.setTitle(R.string.qtn_andr_mng_my_rec_dest_txt);
        	updateUIArrays(recentDestinations);
        }
        managePlacesAdapter = new ManagePlacesAdapter(this, uiItems);
        managePlacesAdapter.notifyDataSetChanged();
//        managePlacesAdapter.sort(new Comparator<PlacesManagerItem>(){
//        	public int compare(PlacesManagerItem l1, PlacesManagerItem l2) {
//        		return l1.getItemName().toLowerCase().compareTo(l2.getItemName().toLowerCase());
//        	}
//        });
        
        listView.setAdapter(managePlacesAdapter);
        listView.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CheckBox box = (CheckBox) view.findViewById(R.id.check_box);
				box.toggle();
				managePlacesAdapter.getItem(position).setChecked(
						box.isChecked());
			}
        });
       
        	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem cmdSelectAll = menu.add(Menu.NONE, MENU_ITEM_SELECT_ALL, Menu.NONE, R.string.qtn_andr_select_all_tk);
		cmdSelectAll.setIcon(R.drawable.cmd_select_all);

		MenuItem cmdRemove = menu.add(Menu.NONE, MENU_ITEM_REMOVE, Menu.NONE, R.string.qtn_andr_remove_tk);
		cmdRemove.setIcon(R.drawable.cmd_remove);
		
		MenuItem cmdUnselectAll = menu.add(Menu.NONE, MENU_ITEM_UNSELECT_ALL, Menu.NONE, R.string.qtn_andr_unselect_all_tk);
		cmdUnselectAll.setIcon(R.drawable.cmd_unselect_all);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_ITEM_SELECT_ALL:
			selectAll();
			return true;
		case MENU_ITEM_REMOVE:
			removeSelected();
			if(places.getSize() == 0){
				finish();
			}
			return true;
		case MENU_ITEM_UNSELECT_ALL:
			unselectAll();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void selectAll(){
		for(int i = 0; i < managePlacesAdapter.getCount(); i++){
			managePlacesAdapter.getItem(i).setChecked(true);
		}
		managePlacesAdapter.notifyDataSetChanged();
	}
	
	private void unselectAll(){
		for(int i = 0; i < managePlacesAdapter.getCount(); i++){
			managePlacesAdapter.getItem(i).setChecked(false);
		}
		managePlacesAdapter.notifyDataSetChanged();
	}
	
	private void removeSelected(){
		for(int i = managePlacesAdapter.getCount() - 1; i >= 0; i--){
			PlacesManagerItem item = managePlacesAdapter.getItem(i);
			if(item.isChecked()){
				if(type == PlacesActivity.TYPE_SAVED_PLACES){
					getApp().removeSavedPlace(item.getFavorite());
				}else {
					getApp().removePreviousDestination(item.getRecentDestination());
		        	
				}
			}
		}
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			places = getApp().getSavedPlaces();
			updateUIArrays(places);
		}else {
			RecentDestination[] places = getApp().getPreviousDestinations();
			updateUIArrays(places);
			
		}
		managePlacesAdapter.notifyDataSetChanged();
	}
	
	private void updateUIArrays(RecentDestination[] destinations) {
		uiItems.clear();
		for(RecentDestination fav : destinations){
			uiItems.add(new PlacesManagerItem(fav, false));
		}
	}
	
	private void updateUIArrays(ListModel listModel) {
		uiItems.clear();
		for(int i = 0; i < listModel.getSize(); i++){
			uiItems.add(new PlacesManagerItem((Favorite)listModel.getElementAt(i), false));
		}
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractActivity#onPause()
	 */
	@Override
	protected void onPause() {
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			getApp().getSavedPlaces().addListDataListener(this);
		}
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractActivity#onResume()
	 */
	@Override
	protected void onResume() {
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			getApp().getSavedPlaces().removeListDataListener(this);
		}
		super.onResume();
	}

	public void contentsChanged(int indexStart, int indexEnd) {
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			updateUIArrays(getApp().getSavedPlaces());
		}
	}

	public void intervalAdded(int indexStart, int indexEnd) {
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			updateUIArrays(getApp().getSavedPlaces());
		}
	}

	public void intervalRemoved(int indexStart, int indexEnd) {
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			updateUIArrays(getApp().getSavedPlaces());
		}
	}
	
	
}
