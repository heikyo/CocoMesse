package com.app.cocomsg;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class CocoMesseActivity extends MapActivity implements LocationListener, OnClickListener{
	
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay myOverlay;
	private LocationManager locationManager;
	private Button twiButton;
	private Button getButton;
	private EditText twiText;
	private double twiLat;
	private double twiLng;
	private String str;
	private ProgressDialog progressDialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        
		AdView adView = (AdView)this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest();
		adView.loadAd(adRequest);

		mapView = (MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(17);
    
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
		getButton = (Button)findViewById(R.id.getbutton);
		getButton.setOnClickListener(this);
		twiButton = (Button)findViewById(R.id.sendbutton);
		twiButton.setOnClickListener(this);
		myOverlay = new MyLocationOverlay(this, mapView);
	}

	

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		GpsCheck();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		twiLat = 0;
		twiLng = 0;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		setPosition(lat, lng);
	}

	private void setPosition(double lat, double lng) {
		this.setPosition(lat, lng, mapView.getZoomLevel());
	}

	private void setPosition(double lat, double lng, int zoom) {
		GeoPoint p = new GeoPoint((int)(lat*1E6), (int)(lng*1E6));
		mapController.animateTo(p);
		mapController.setZoom(zoom);
		twiLat = (int)(lat*1E6);
		twiLng = (int)(lng*1E6);
	}

	@Override
	public void onProviderDisabled(String provider) {
	
	}

	@Override
	public void onProviderEnabled(String provider) {
	
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	
	}

	private void getMyLocation() {
		setProgressDialog();
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				myOverlay.enableMyLocation();
				myOverlay.runOnFirstFix(new Runnable() {
					public void run() {
						mapController.animateTo(myOverlay.getMyLocation());
						mapController.setZoom(mapView.getZoomLevel());
						mapView.getOverlays().add(myOverlay);
						twiLat = myOverlay.getMyLocation().getLatitudeE6();
						twiLng = myOverlay.getMyLocation().getLongitudeE6();
						progressDialog.dismiss();
					}
				});
				Looper.loop();	
			}
		}.start();
	}

	private void twitter() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.message);
		twiText = new EditText(this);
		builder.setView(twiText);
		builder.setNeutralButton(R.string.phrase, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder builder = new AlertDialog.Builder(CocoMesseActivity.this);
				final String[] item = getResources().getStringArray(R.array.default_value);
				builder.setTitle(R.string.phrase);
				builder.setItems(item, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						str = item[which];
						sendIntent(str);
					}
				});
				builder.create().show();
			}
		});
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				str = twiText.getText().toString();
				sendIntent(str);
			}
		});
		builder.create().show();
	}

	private void sendIntent(String str) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		String url = "http://maps.google.co.jp/maps?q=" + twiLat/1E6 + "," + twiLng/1E6;
		String shortUrl = Bitly.shorten(url);
		intent.putExtra(Intent.EXTRA_TEXT, str + ": " + shortUrl);
		startActivity(intent);
	}

	private void setProgressDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		String message = getString(R.string.getrlocation);
		progressDialog.setMessage(message);
		progressDialog.show();
	}

	private void GpsCheck() {
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.gps);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent SettingGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(SettingGps);
				}
			});
			builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.create().show();
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(getApplicationContext(), R.string.pleaselocation, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.getbutton:
			getMyLocation();
			break;
		case R.id.sendbutton:
			if(twiLat == 0 || twiLng == 0) {
				Toast.makeText(getApplicationContext(), R.string.location, Toast.LENGTH_LONG).show();
				getMyLocation();
			} else {
				twitter();
			}
			break;
		}
	}
}
