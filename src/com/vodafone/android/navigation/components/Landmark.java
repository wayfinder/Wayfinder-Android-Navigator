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

import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch;
import com.wayfinder.core.shared.Position;

public class Landmark {
	private RecentDestination place;
	private SearchMatch match;
	private Favorite favorite;
	private int type;
	
	private static final int TYPE_SEARCH_MATCH = 0;
	private static final int TYPE_RECENT_DESTINATION = 1;
	private static final int TYPE_FAVORITE = 2;
	public Landmark(RecentDestination place){
		this.place = place;
		this.type = TYPE_RECENT_DESTINATION;
	}
	
	public Landmark(SearchMatch match){
		this.match = match;
		this.type = TYPE_SEARCH_MATCH;
	}
	
	public Landmark(Favorite favorite){
		this.favorite = favorite;
		this.type = TYPE_FAVORITE;
	}
	
	public String getName(){
		if(type == TYPE_RECENT_DESTINATION){
			return place.getName();
		} else if(type == TYPE_SEARCH_MATCH){
			return match.getMatchName();
		} else {
			return favorite.getName();
		}
	}
	
	public String getDescription(){
		if(type == TYPE_RECENT_DESTINATION){
			return place.getDescription();
		} else if(type == TYPE_SEARCH_MATCH){
			return match.getMatchLocation();
		} else {
			return favorite.getDescription();
		}
	}
	
	public String getImageName(){
		if(type == TYPE_RECENT_DESTINATION){
			return place.getIconName();
		} else if(type == TYPE_SEARCH_MATCH){
			String image = match.getMatchBrandImageName();
			if(image.length() == 0) {
				image = match.getMatchCategoryImageName();
			}
			if(image.length() == 0) {
				image = match.getMatchProviderImageName();
			}
			return image;
		} else {
			return favorite.getIconName();
		}
	}
	
	public Position getPosition(){
		if(type == TYPE_RECENT_DESTINATION){
			return place.getPosition();
		} else if(type == TYPE_SEARCH_MATCH){
			return match.getPosition();
		} else {
			return favorite.getPosition();
		}
	}
	
	public String getId(){
		if(type == TYPE_SEARCH_MATCH){
			return match.getMatchID();
		} else {
			return "";
		}
	}
	
	public long getTimestamp(){
		if(type == TYPE_RECENT_DESTINATION){
			return place.getTimestamp();
		} else {
			return 0;
		}
	}
	
	public long getPlaceDBId(){
		if(type == TYPE_RECENT_DESTINATION){
			return place.getId();
		} else {
			return 0;
		}
	}

	
	public Favorite getFavorite(){
		return favorite;
	}
	
}
