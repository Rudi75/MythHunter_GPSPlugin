package com.mythHunter.app;

import java.util.Locale;

import com.unity3d.player.UnityPlayerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class GPSPlugin extends UnityPlayerActivity {

	static String TAG = "GPSPlugin";
	/** Stores the current location */
	public static Location currentGPSLocation = null;
	public static Location currentNetworkLocation = null;

	public static LocationManager locationManager;

	/** Listeners for the gps and network location */
	LocationListener networkLocationListener;
	LocationListener gpsLocationListener;

	static boolean network_enabled = false;
	static boolean gps_enabled = false;

	@Override
	protected void onCreate(Bundle myBundle) {
		super.onCreate(myBundle);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Starts the listeners
		startLocationListeners();
	}

	@Override
	protected void onResume() {

		super.onResume();
		startLocationListeners();
	}

	@Override
	protected void onPause() {
		locationManager.removeUpdates(networkLocationListener);
		locationManager.removeUpdates(gpsLocationListener);

		super.onPause();

	}

	@Override
	protected void onStop() {
		locationManager.removeUpdates(networkLocationListener);
		locationManager.removeUpdates(gpsLocationListener);
		super.onStop();
	}

	public void startLocationListeners() {
		Log.i(TAG, "startLocationListeners");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		gpsLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				Log.i(TAG, "GPS update " + location.toString());
				currentGPSLocation = location;
			}

			public void onProviderDisabled(String provider) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		};

		networkLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				currentNetworkLocation = location;
			}

			public void onProviderDisabled(String provider) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		};

		try {
			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				
				String language = Locale.getDefault().getLanguage();
				String message = "This application needs GPS. Enable it?";
				String okText = "OK";
				String cancelText = "Cancel";
				if(language.equals("de"))
				{
					message = "Diese App benoetigt GPS. Einschalten?";
					okText = "OK";
					cancelText = "Abbrechen";
				}
				displayPromptForEnablingGPS(this, message, okText, cancelText);
			}
			gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			Log.i(TAG, ex.getMessage());
		}

		try {
			network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			Log.i(TAG, ex.getMessage());
		}
		Log.i(TAG, "gps_enabled " + gps_enabled + " net_enabled " + network_enabled);
		if (gps_enabled) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
		}

		if (network_enabled) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
		}
	}

	public static String getLocation() {
		if (currentGPSLocation == null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			currentGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}

		if (currentNetworkLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			currentNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (currentGPSLocation != null && (currentNetworkLocation == null
				|| (currentGPSLocation.getTime() >= currentNetworkLocation.getTime() - 10000))) {
			Log.i(TAG, "return GPS: " + currentGPSLocation.toString());
			return currentGPSLocation.getLongitude() + ";" + currentGPSLocation.getLatitude();
		} else if (currentNetworkLocation != null) {
			Log.i(TAG, "return NET: " + currentNetworkLocation.toString());
			return currentNetworkLocation.getLongitude() + ";" + currentNetworkLocation.getLatitude();
		} else {
			return null;
		}
	}

	private void displayPromptForEnablingGPS(final Activity activity, String message, String okText,
			String cancelText) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
		builder.setMessage(message).setPositiveButton(okText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int id) {
				activity.startActivity(new Intent(action));
				d.dismiss();
			}
		}).setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int id) {
				d.cancel();
			}
		});
		builder.create().show();
	}

	public static boolean isNetwork_enabled() {
		return network_enabled;
	}

	public static boolean isGps_enabled() {
		Log.i(TAG, "gps_enabled " + gps_enabled);
		return gps_enabled;
	}

}