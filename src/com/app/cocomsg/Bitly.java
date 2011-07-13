package com.app.cocomsg;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public class Bitly {
	private static final String USERNAME = "heikyo";
	private static final String APIKEY = "R_f012fa4a8ed16b9096ebbaf7dcc84284";
	
	public static String shorten(String longUrl) {
		String shortUrl = "";
		String login = USERNAME;
		String apiKey = APIKEY;
		
		Uri.Builder builder = new Uri.Builder();
		builder.path("http://api.bit.ly/v3/shorten");
		builder.appendQueryParameter("login", login);
		builder.appendQueryParameter("apiKey", apiKey);
		builder.appendQueryParameter("longUrl", longUrl);
		builder.appendQueryParameter("format", "json");
		String uri = Uri.decode(builder.build().toString());
		
		try {
			HttpUriRequest httpGet = new HttpGet(uri);
			DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
			HttpResponse httpResponses = defaultHttpClient.execute(httpGet);
			if(httpResponses.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String entity = EntityUtils.toString(httpResponses.getEntity());
				JSONObject jsonEntity = new JSONObject(entity);
				if(jsonEntity != null) {
					JSONObject jsonResults = jsonEntity.getJSONObject("data");
					if(jsonResults != null) {
						shortUrl = jsonResults.getString("url");
					}
				}
			}
		} catch (IOException io) {
			io.printStackTrace();
		} catch (JSONException json) {
			json.printStackTrace();
		}
		return shortUrl;
	}

}
