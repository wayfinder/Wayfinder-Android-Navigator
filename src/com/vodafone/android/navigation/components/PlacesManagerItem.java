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
package com.vodafone.android.navigation.components;

import com.vodafone.android.navigation.activity.PlacesActivity;
import com.wayfinder.core.favorite.Favorite;



public class PlacesManagerItem {
	private boolean checked;
	private RecentDestination destination;
	private Favorite favorite;
	int type;

	public PlacesManagerItem(RecentDestination destination, boolean checked) {
		this.checked = checked;
		this.destination = destination;
		type = PlacesActivity.TYPE_DESTIONATIONS;
	}
	
	public PlacesManagerItem(Favorite favorite, boolean checked) {
		this.checked = checked;
		this.favorite = favorite;
		type = PlacesActivity.TYPE_SAVED_PLACES;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public RecentDestination getRecentDestination() {
		return destination;
	}
	
	public Favorite getFavorite(){
		return favorite;
	}
	
	public String getItemName(){
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			return favorite.getName();
		} else {
			return destination.getName();
		}
	}
	
	public String getItemDescription(){
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			return favorite.getDescription();
		} else {
			return destination.getDescription();
		}
	}
	
	public String getItemImageName(){
		if(type == PlacesActivity.TYPE_SAVED_PLACES){
			return favorite.getIconName();
		} else {
			return destination.getIconName();
		}
	}
}
