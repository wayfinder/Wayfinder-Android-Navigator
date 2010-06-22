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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.Log;

import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.wayfinder.pal.android.AndroidPAL;

/**
 * Handles auto-update functionality
 * 
 */
public class AutoUpdater {

	private static final String LOG_TAG = AutoUpdater.class.getName();

	/**
	 * Setting key for:
	 * <p>
	 * Base URL for Android Market.
	 * <p>
	 * If not explicitly specified, the default value is used ("market://")
	 * 
	 * @see {@link ApplicationSettings#DEFAULT_MARKET_BASE}
	 */
	public static Integer SETTING_MARKET_BASE = 1;

	/**
	 * Setting key for:
	 * <p>
	 * URL for Android Market search by package name
	 * <p>
	 * If not explicitly specified, the default value is used
	 * ("search?q=pname:")
	 * 
	 * @see {@link ApplicationSettings#DEFAULT_MARKET_PACKAGE_SEARCH}
	 */
	public static Integer SETTING_MARKET_PACKAGE_SEARCH = SETTING_MARKET_BASE + 1;

	/**
	 * Setting key for:
	 * <p>
	 * URL for Android Market search by application name
	 * <p>
	 * If not explicitly specified, the default value is used ("search?q=")
	 * 
	 * @see {@link ApplicationSettings#DEFAULT_MARKET_NAME_SEARCH}
	 */
	public static Integer SETTING_MARKET_NAME_SEARCH = SETTING_MARKET_PACKAGE_SEARCH + 1;

	/**
	 * Setting key for:
	 * <p>
	 * Vodafone client id.
	 * <p>
	 * Must be explicitly set at the constructor.
	 * 
	 * @see {@link #AutoUpdater(Context, Map)}
	 */
	public static Integer SETTING_CLIENT_ID = SETTING_MARKET_NAME_SEARCH + 1;

	/**
	 * Setting key for:
	 * <p>
	 * URL pointing to the location base of the version and market files on the
	 * server.
	 * <p>
	 * Must be explicitly set at the constructor.
	 * <p>
	 * The complete version/market file URL will be constructed from this base
	 * url + client id + application package name + version/market file name.
	 * 
	 * @see {@link #AutoUpdater(Context, Map)}
	 * @see {@link #SETTING_CLIENT_ID}
	 * @see {@link #SETTING_VERSION_CHECK}
	 * @see {@link #SETTING_MARKET_CHECK}
	 */
	public static Integer SETTING_VERSION_BASE = SETTING_CLIENT_ID + 1;

	/**
	 * Setting key for:
	 * <p>
	 * Name of the version file.
	 * <p>
	 * Must be explicitly set at the constructor.
	 * 
	 * @see {@link #AutoUpdater(Context, Map)}
	 */
	public static Integer SETTING_VERSION_CHECK = SETTING_VERSION_BASE + 1;

	/**
	 * Setting key for:
	 * <p>
	 * Name of the market file containing the market base URL.
	 * <p>
	 * Must be explicitly set at the constructor.
	 * 
	 * @see {@link #AutoUpdater(Context, Map)}
	 */
	public static Integer SETTING_MARKET_CHECK = SETTING_VERSION_CHECK + 1;

	private Context ctx;

	private Map<Integer, String> params;

	/**
	 * Constructs an {@link AutoUpdater} object.
	 * 
	 * @param context
	 *            The context is needed to determine the application package
	 *            name, version and to get hold of an {@link AndroidPAL}
	 *            instance.
	 * @param settings
	 *            The following settings MUST be specified:
	 *            {@link #SETTING_CLIENT_ID}, {@link #SETTING_VERSION_BASE},
	 *            {@link #SETTING_VERSION_CHECK}, {@link #SETTING_MARKET_CHECK}
	 * 
	 * @throws NullPointerException
	 *             if the context or the settings object is null
	 * @throws IllegalStateException
	 *             if the required settings aren't specified
	 */
	public AutoUpdater(Context context, Map<Integer, String> settings) {
		if (context == null) {
			throw new NullPointerException(
					"'context' parameter shouldn't be null");
		}

		if (settings == null) {
			throw new NullPointerException(
					"'settings' parameter shouldn't be null");
		}

		if (!settings.containsKey(SETTING_MARKET_BASE)) {
			settings.put(SETTING_MARKET_BASE,
					ApplicationSettings.DEFAULT_MARKET_BASE);
		} else if (!settings.get(SETTING_MARKET_BASE).trim().endsWith("//")) {
			settings.put(SETTING_MARKET_BASE, settings.get(
					ApplicationSettings.DEFAULT_MARKET_BASE).trim()
					+ "//");
		}

		if (!settings.containsKey(SETTING_MARKET_PACKAGE_SEARCH)) {
			settings.put(SETTING_MARKET_PACKAGE_SEARCH,
					ApplicationSettings.DEFAULT_MARKET_PACKAGE_SEARCH);
		}

		if (!settings.containsKey(SETTING_MARKET_NAME_SEARCH)) {
			settings.put(SETTING_MARKET_NAME_SEARCH,
					ApplicationSettings.DEFAULT_MARKET_NAME_SEARCH);
		}

		if (!settings.containsKey(SETTING_CLIENT_ID)) {
			throw new IllegalStateException(
					"SETTING_CLIENT_ID setting should be specified");
		}

		if (!settings.containsKey(SETTING_VERSION_BASE)) {
			throw new IllegalStateException(
					"SETTING_VERSION_BASE setting should be specified");
		} else {
			String versionBase = settings.get(SETTING_VERSION_BASE).trim();

			if (versionBase.endsWith("/") || versionBase.endsWith("\\")) {
				settings.put(SETTING_VERSION_BASE, versionBase.substring(0,
						versionBase.length() - 1));
			}
		}

		if (!settings.containsKey(SETTING_VERSION_CHECK)) {
			throw new IllegalStateException(
					"SETTING_VERSION_CHECK setting should be specified");
		} else {
			String versionCheck = settings.get(SETTING_VERSION_CHECK).trim();

			if (versionCheck.startsWith("/") || versionCheck.startsWith("\\")) {
				settings.put(SETTING_VERSION_CHECK, versionCheck.substring(1,
						versionCheck.length()));
			}
		}

		if (!settings.containsKey(SETTING_MARKET_CHECK)) {
			throw new IllegalStateException(
					"SETTING_MARKET_CHECK setting should be specified");
		} else {
			String marketCheck = settings.get(SETTING_MARKET_CHECK).trim();

			if (marketCheck.startsWith("/") || marketCheck.startsWith("\\")) {
				settings.put(SETTING_MARKET_CHECK, marketCheck.substring(1,
						marketCheck.length()));
			}
		}

		this.ctx = context;
		this.params = settings;
	}

	/**
	 * Fetches the version file from the following location:
	 * <p>
	 * {@link #SETTING_VERSION_BASE} + '/' + {@link #SETTING_CLIENT_ID} + '/' +
	 * (application package name) + '/' + {@link #SETTING_VERSION_CHECK}
	 * <p>
	 * and returns the version but only if it's higher than the current version.
	 * <p>
	 * The version file should have the latest version name specified in it's
	 * first (and only) row in the following (regex) format:
	 * [\d]+[\.][\d]+[\.][\d]+
	 * <p>
	 * For example: 9.3.6
	 * 
	 * @return Version name if a version has been found that is higher than the
	 *         current one, null otherwise. The method also returns null in
	 *         cases of communication error or if the fetched version has the
	 *         wrong format.
	 * 
	 * @see VersionUtil#compareVersions(String, String)
	 */
	public String checkVersion() {
		BufferedReader in = null;
		HttpURLConnection conn = null;

		String versionFileURL = getSetting(SETTING_VERSION_BASE) + "/"
				+ getSetting(SETTING_CLIENT_ID) + "/" + ctx.getPackageName()
				+ "/" + getSetting(SETTING_VERSION_CHECK);

		try {
			conn = getURLConnection(versionFileURL);
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String serverVersion = in.readLine();
			String clientVersion = getVersion();

			if (VersionUtil.compareVersions(serverVersion, clientVersion) > 0) {
				return serverVersion;
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Version check: ", e);
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * Returns a setting
	 * 
	 * @param key
	 *            Setting key
	 * 
	 * @return setting value as specified with the constructor's 'settings'
	 *         parameter
	 * @see #AutoUpdater(Context, Map)
	 */
	public String getSetting(int key) {
		return params.get(key).trim();
	}

	/**
	 * Generates a HTTP connection object from the specified <code>url</code>
	 * with the necessary header settings.
	 * 
	 * @param url
	 *            absolute URL
	 * 
	 * @return {@link HttpURLConnection} instance
	 */
	private HttpURLConnection getURLConnection(String url) throws IOException {
		URL _url = null;
		HttpURLConnection conn = null;
		try {
			AndroidPAL androidPAL = AndroidPAL.createAndroidPAL(ctx);
			Proxy proxy = androidPAL.getHttpConfiguration().getProxy();
			_url = new URL(url);

			if (proxy == Proxy.NO_PROXY) {
				conn = (HttpURLConnection) _url.openConnection();
			} else {
				conn = (HttpURLConnection) _url.openConnection(proxy);
			}
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");
			conn.addRequestProperty("Accept", "*/*");
			conn.addRequestProperty("User-Agent", getSetting(SETTING_CLIENT_ID)
					+ "/" + getVersion());
			conn.connect();
			int code = conn.getResponseCode();
			Log.d("NavigatorApplication", "response " + code);

			return conn;
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Returns a URL that points to the Android Market search page with the
	 * search parameters set for "package-search".
	 * <p>
	 * The URL is constructed from the base market url, the
	 * {@link #SETTING_MARKET_PACKAGE_SEARCH} value and the application package
	 * name.
	 * <p>
	 * The base Market URL is fetched from the server through the following URL:
	 * <p>
	 * {@link #SETTING_VERSION_BASE} + '\' + {@link #SETTING_CLIENT_ID} + '\' +
	 * (application package name) + '\' + {@link #SETTING_MARKET_CHECK}
	 * <p>
	 * If the base Market URL cannot be fetched for any reason, the default
	 * value is used
	 * 
	 * @return URL that - in a browser - will open the Market web-page and
	 *         initiate a search with the current package name
	 * 
	 * @see #SETTING_MARKET_BASE
	 * @see #getMarketUrlBase()
	 */
	public String getMarketUrlForPackageSearch() {
		String url = null;

		url = getMarketUrlBase();

		if (url == null) {
			url = getSetting(SETTING_MARKET_BASE);
		}

		return url + getSetting(SETTING_MARKET_PACKAGE_SEARCH)
				+ ctx.getPackageName();
	}

	/**
	 * Returns a URL that points to the Android Market search page with the
	 * search parameters set for "name-search".
	 * <p>
	 * The URL is constructed from the base market url, the
	 * {@link #SETTING_MARKET_NAME_SEARCH} value and the application name.
	 * <p>
	 * The base Market URL is fetched from the server through the following URL:
	 * <p>
	 * {@link #SETTING_VERSION_BASE} + '\' + {@link #SETTING_CLIENT_ID} + '\' +
	 * (application package name) + '\' + {@link #SETTING_MARKET_CHECK}
	 * <p>
	 * If the base Market URL cannot be fetched for any reason, the default
	 * value is used
	 * 
	 * @return URL that - in a browser - will open the Market web-page and
	 *         initiate a search with the current package name
	 * 
	 * @see #SETTING_MARKET_BASE
	 * @see #getMarketUrlBase()
	 * @see #getAppName()
	 */
	public String getMarketUrlForNameSearch() {
		String url = null;

		url = getMarketUrlBase();

		if (url == null) {
			url = getSetting(SETTING_MARKET_BASE);
		}

		return url + getSetting(SETTING_MARKET_NAME_SEARCH) + getAppName();
	}

	/**
	 * Fetches the Android Market base URL from the following location:
	 * <p>
	 * {@link #SETTING_VERSION_BASE} + "/" + {@link #SETTING_CLIENT_ID} + "/" +
	 * (application package name) + {@link #SETTING_MARKET_CHECK}
	 * 
	 * @return Market base URL if it has been found, null otherwise
	 */
	private String getMarketUrlBase() {
		BufferedReader in = null;
		HttpURLConnection conn = null;

		String marketFileURL = getSetting(SETTING_VERSION_BASE) + "/"
				+ getSetting(SETTING_CLIENT_ID) + "/" + ctx.getPackageName()
				+ "/" + getSetting(SETTING_MARKET_CHECK);

		try {
			conn = getURLConnection(marketFileURL);
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			return in.readLine();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Market url check: ", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return null;
	}

	/**
	 * Fetches the current version name (the value of the AndroidManifest.xml's
	 * android:versionName parameter).
	 * 
	 * @return current version name if it can be fetched, 0.0.0 otherwise
	 */
	private String getVersion() {
		String version = "0.0.0";
		try {
			PackageManager packageManager = ctx.getPackageManager();

			if (packageManager != null) {
				PackageInfo packageInfo = packageManager.getPackageInfo(ctx
						.getPackageName(), 0);

				if (packageInfo != null) {
					version = packageInfo.versionName;
				}
			}
		} catch (NameNotFoundException e) {
		}
		return version;
	}

	/**
	 * Fetches the current application name from the AndroidManifest.xml's
	 * android:label attribute. If it's localized it will use the value from the
	 * respective string-resource file, otherwise whatever's the value of the
	 * attribute
	 * 
	 * @return current application name
	 */
	private String getAppName() {
		String name = null;
		PackageManager packageManager = ctx.getPackageManager();
		ApplicationInfo applicationInfo = null;

		if (packageManager != null) {
			try {
				applicationInfo = packageManager.getApplicationInfo(ctx
						.getPackageName(), 0);

				if (applicationInfo != null) {
					name = ctx.getResources().getString(
							applicationInfo.labelRes);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			} catch (Resources.NotFoundException e) {
				e.printStackTrace();
				name = applicationInfo.nonLocalizedLabel.toString();
			}
		}
		return name;
	}
}
