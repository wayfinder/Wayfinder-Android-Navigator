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
package com.vodafone.android.navigation.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle version related operations
 * 
 */
public class VersionUtil {

	/**
	 * Compares versions
	 * 
	 * @param version1
	 *            regex syntax: [\d]+[\.][\d]+[\.][\d]+
	 * @param version2
	 *            regex syntax: [\d]+[\.][\d]+[\.][\d]+
	 * @return a negative integer if version1 is less than version2; a positive
	 *         integer if version1 is greater than version2; 0 if version1
	 *         equals version2.
	 * @throws Exception
	 *             if the specified versions don't have the correct syntax
	 */
	public static int compareVersions(String version1, String version2)
			throws Exception {
		Integer majVer1 = null;
		Integer medVer1 = null;
		Integer minVer1 = null;
		Integer majVer2 = null;
		Integer medVer2 = null;
		Integer minVer2 = null;
		
		Integer compare;

		Pattern versionPattern = Pattern
				.compile("([\\d]+)[\\.]([\\d]+)[\\.]([\\d]+)");
		Matcher matcher1 = versionPattern.matcher(version1);
		Matcher matcher2 = versionPattern.matcher(version2);

		if (!matcher1.find() || !matcher2.find() || matcher1.groupCount() != 3
				|| matcher2.groupCount() != 3) {
			throw new Exception("Invalid versions: " + version1 + " "
					+ version2);
		} else {
			majVer1 = Integer.parseInt(matcher1.group(1));
			medVer1 = Integer.parseInt(matcher1.group(2));
			minVer1 = Integer.parseInt(matcher1.group(3));

			majVer2 = Integer.parseInt(matcher2.group(1));
			medVer2 = Integer.parseInt(matcher2.group(2));
			minVer2 = Integer.parseInt(matcher2.group(3));

			compare = 0;
			
			if (majVer1 != null && majVer2 != null) {
				compare = majVer1 - majVer2;

				if (compare == 0) {
					if (medVer1 != null && medVer2 != null) {
						compare = medVer1 - medVer2;

						if (compare == 0) {
							if (minVer1 != null && minVer2 != null) {
								compare = minVer1 - minVer2;
							}
						}
					} else {
						if (minVer1 != null && minVer2 != null) {
							compare = minVer1 - minVer2;
						}
					}
				}
			} else
				throw new Exception("Invalid versions: " + version1 + " "
						+ version2);
		}

		return compare;
	}
}
