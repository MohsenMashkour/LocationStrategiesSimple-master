package com.mr.locationstrategiessimple;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Locale;



public class MainActivity extends AppCompatActivity implements LocationListener {
	String LOG_TAG = getClass().getSimpleName();
	private int MY_PERMISSION_REQUEST_ACCESS = 101;
	private final String FUSED_PROVIDER = "fused";
	private View rootView;
	TextView locationProviderTxtV;
	TextView locationInfoTxtV;
	ToggleButton startStopButton;
	LocationManager locationManager;
	private String currentLocationProvider = LocationManager.GPS_PROVIDER;
	private boolean locationPermission;
	private boolean receivingLocationUpdates = false;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Create an instance of FusedLocationProviderClient
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		rootView = findViewById(R.id.rootView);
		startStopButton = findViewById(R.id.startStopButton);
		startStopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
				if (!locationPermission)
					checkPermission();
				if (isChecked) {
					// register listener
					connectLocationListener();
				} else {
					// unregister listener
					disconnectLocationListener();
				}
			}
		});
		locationInfoTxtV = findViewById(R.id.locationInfoTxtV);
		locationProviderTxtV = findViewById(R.id.locationStrategyTxtV);
		locationProviderTxtV.setText(currentLocationProvider);
		checkPermission();


	}

	@Override
	protected void onStop() {
		super.onStop();
		disconnectLocationListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.menu_gps:
				if (!currentLocationProvider.equals(LocationManager.GPS_PROVIDER) && receivingLocationUpdates) {
					startStopButton.setChecked(false);
				}
				currentLocationProvider = LocationManager.GPS_PROVIDER;
				break;
			case R.id.menu_cellular:
				if (!currentLocationProvider.equals(LocationManager.NETWORK_PROVIDER) && receivingLocationUpdates) {
					startStopButton.setChecked(false);
				}
				currentLocationProvider = LocationManager.NETWORK_PROVIDER;
				break;
			case R.id.menu_fused:
				if (!currentLocationProvider.equals(FUSED_PROVIDER) && receivingLocationUpdates) {
					startStopButton.setChecked(false);
				}
				currentLocationProvider = FUSED_PROVIDER;
				break;
			default:
		}
		locationProviderTxtV.setText(currentLocationProvider);
		return super.onOptionsItemSelected(item);
	}

	protected void checkPermission() {
		// Check the required permissions
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// Request the missing permissions
			requestPermission();
			return;

		} else {
			locationPermission = true;
		}

	}

	protected void requestPermission() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_ACCESS);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == MY_PERMISSION_REQUEST_ACCESS) {
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// permission was granted!
				locationPermission = true;
			} else {
				// permission denied
				Snackbar.make(rootView, "Permission is required to continue", Snackbar.LENGTH_LONG)
						.setAction("RETRY", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// Retry permission request
								requestPermission();
							}
						}).show();
				locationPermission = false;
			}
		}
	}


	@SuppressLint("MissingPermission")
	private void connectLocationListener() {
		long updateInterval = 1000; // Interval of location updates in ms
		if (!currentLocationProvider.equals(FUSED_PROVIDER)) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(currentLocationProvider, updateInterval, 0, this);
		} else {
			// register fused provider
			createLocationCallback();
			LocationRequest locationRequest = new LocationRequest();
			locationRequest.setInterval(updateInterval); // Interval of location updates
			locationRequest.setFastestInterval(500);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
		}
		receivingLocationUpdates = true;
	}

	private void disconnectLocationListener() {
		if (!currentLocationProvider.equals(FUSED_PROVIDER)) {
			// Remove the listener you previously added
			locationManager.removeUpdates(this);
		} else {
			// unregister fused provider
			stopLocationUpdates();
		}
		receivingLocationUpdates = false;
	}

	private void createLocationCallback() {
		// create the locationCallback
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				// Code executed when user's location changes
				if (locationResult != null) {
					// Remove the last reported location
					Location location = locationResult.getLastLocation();
					locationInfoTxtV.setText(locationToString(location));
				}
			}
		};
	}

	private void stopLocationUpdates() {
		if (locationCallback != null)
			fusedLocationClient.removeLocationUpdates(locationCallback);
	}

	@Override
	public void onLocationChanged(Location location) {
		String info = locationToString(location);
		locationInfoTxtV.setText(info);
	}

	private String locationToString(Location location) {
		Calendar locationDate = Calendar.getInstance();
		locationDate.setTimeInMillis(location.getTime());
		return String.format(Locale.getDefault(),
				"Lat: %.2f, Lon: %.2f, Accurracy: %.2f, time: %d (%02d.%02d.%d %02d:%02d:%02d)",
				location.getLatitude(),location.getLongitude(),location.getAccuracy(),location.getTime(),
				locationDate.get(Calendar.DAY_OF_MONTH),locationDate.get(Calendar.MONTH),locationDate.get(Calendar.YEAR),
				locationDate.get(Calendar.HOUR_OF_DAY),locationDate.get(Calendar.MINUTE),locationDate.get(Calendar.SECOND));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(LOG_TAG, provider + getResources().getString(R.string.status_msg));
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i(LOG_TAG, currentLocationProvider + getResources().getString(R.string.enabled_msg));
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(LOG_TAG, currentLocationProvider + getResources().getString(R.string.disabled_msg));
	}
}
