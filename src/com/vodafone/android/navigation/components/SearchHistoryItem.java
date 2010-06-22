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

import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryCollection;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.TopRegionCollection;


public class SearchHistoryItem {
	
	private static final int INVALID_ID = -1;
	
	private long id;
	private long timestamp;
	private int topRegionId = INVALID_ID;
	private int categoryId = INVALID_ID;
	private String whatText;
	private String whereText;
	private Category category;
	private TopRegion topRegion;
	
	public SearchHistoryItem(long id, String whatText, String whereText, int topRegionId, int categoryId, long timestamp){
		this.id = id;
		this.whatText = whatText;
		this.whereText = whereText;
		this.topRegionId = topRegionId;
		this.categoryId = categoryId;
		this.timestamp = timestamp;
	}
	
	public SearchHistoryItem(SearchQuery query, long timestamp){
		this.whatText = query.getItemQueryStr();
		this.whereText = query.getSearchAreaStr();
		if(query.getTopRegion() != null){
			this.topRegionId = query.getTopRegion().getRegionID();
		}
		if(query.getCategory() != null){
			this.categoryId = query.getCategory().getCategoryID();
		}
		this.timestamp = timestamp;
	}
	
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the topRegionId
	 */
	public int getTopRegionId() {
		return topRegionId;
	}

	/**
	 * @return the categoryId
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * @return the whatText
	 */
	public String getWhatText() {
		return whatText;
	}

	/**
	 * @return the whereText
	 */
	public String getWhereText() {
		return whereText;
	}

	public void setupInternalData(TopRegionCollection topRegionCollection, CategoryCollection categoryCollection) {
	    if(categoryCollection != null) {
	        category = categoryCollection.getCategoryByID(categoryId);
	    }
	    if(topRegionCollection != null) {
	        topRegion = topRegionCollection.getTopRegionByID(topRegionId);
	    }
	}

	/**
	 * @return the category
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * @return the topRegion
	 */
	public TopRegion getTopRegion() {
		return topRegion;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	public boolean hasSameValues(SearchHistoryItem item) {
		return categoryId == item.getCategoryId()
		    && topRegionId == item.getTopRegionId()
		    && whatText.equals(item.getWhatText())
		    && whereText.equals(item.getWhereText());
	}
}
