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
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;

public class RecentDestination implements Favorite {

	public static final int INVALID_ID = -1;

	private long id = INVALID_ID;
	private Position position;
	private String name;
	private String description;
	private long timestamp;
	private String imageName;
	private InfoFieldList infoFieldList;

	public RecentDestination(String name, String description,
			Position position, long timestamp, String imageName,
			InfoFieldList infoFieldList) {
		this.name = name;
		this.description = description;
		this.position = position;
		this.timestamp = timestamp;
		this.imageName = imageName;
		this.infoFieldList = infoFieldList;
	}

	public RecentDestination(long id, String name, String description,
			Position position, long timestamp, String imageName,
			InfoFieldList infoFieldList) {
		this(name, description, position, timestamp, imageName, infoFieldList);
		this.id = id;
	}

	public RecentDestination(RecentDestination rescentDestination) {
		this(rescentDestination.getName(), rescentDestination.getDescription(),
				rescentDestination.getPosition(), System.currentTimeMillis(),
				rescentDestination.getIconName(), rescentDestination
						.getInfoFieldList());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String aDescription) {
		this.description = aDescription;
	}

	public String getName() {
		return name;
	}

	public void setName(String aName) {
		this.name = aName;
	}

	public InfoFieldList getInfoFieldList() {
		return infoFieldList;
	}

	public Position getPosition() {
		return position;
	}

	public long getId() {
		return id;
	}

	public void setId(long aId) {
		this.id = aId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getIconName() {
		return imageName;
	}

	// public boolean isSameAs(RecentDestination otherDestination) {
	// return (this.position == otherDestination.position || (this.position
	// .getMc2Latitude() == otherDestination.position.getMc2Latitude() &&
	// this.position
	// .getMc2Longitude() == otherDestination.position
	// .getMc2Longitude()))
	// && (this.name == otherDestination.name || this.name
	// .equals(otherDestination.name))
	// && (this.description == otherDestination.getDescription() ||
	// this.description
	// .equals(otherDestination.description))
	// && (this.imageName == otherDestination.imageName || this.imageName
	// .equalsIgnoreCase(otherDestination.imageName));
	// }

	public boolean isSameAs(RecentDestination obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (description == null) {
			if (obj.description != null) {
				return false;
			}
		} else if (!description.equals(obj.description)) {
			return false;
		}

		if (id != obj.id) {
			return false;
		}

		if (imageName == null) {
			if (obj.imageName != null) {
				return false;
			}
		} else if (!imageName.equals(obj.imageName)) {
			return false;
		}

		if (infoFieldList == null) {
			if (obj.infoFieldList != null) {
				return false;
			}
		} else if (!infoFieldList.equals(obj.infoFieldList)) {
			return false;
		}

		if (name == null) {
			if (obj.name != null)
				return false;
		} else if (!name.equals(obj.name)) {
			return false;
		}

		if (position == null) {
			if (obj.position != null)
				return false;
		} else if (this.position.getMc2Latitude() != obj.position
				.getMc2Latitude()
				|| this.position.getMc2Longitude() != obj.position
						.getDecimalLongitude()) {
			return false;
		}
		return true;
	}
}
