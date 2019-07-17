package com.mr.locationstrategiessimple;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Locale;



public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, NetworkListener.NetworkStateChangedListener {

	//Map
	private GoogleMap mMap;
    Marker marker;

    //FireBase
	DatabaseReference databaseReference;
	DataBase dataBase;

	//Cell info
	TelephonyManager telephonyManager;
	TextView signalStrengthTextView;


    //LocationTechniques
    String LOG_TAG = getClass().getSimpleName();
	private int MY_PERMISSION_REQUEST_ACCESS = 101;
	private final String FUSED_PROVIDER = "fused";
	private View rootView;
	TextView locationProviderTxtV;
	TextView locationInfoTxtV;
	ToggleButton startStopButton;
	EditText editTextMemo;
	LocationManager locationManager;
	private String currentLocationProvider = LocationManager.GPS_PROVIDER;
	private boolean locationPermission;
	private boolean receivingLocationUpdates = false;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private int lteSignalStrength = -1;
	private int gsmSignalStrength = -1;
	private int cdmaDbm = -1;
    private int lteCqi = -1;
    private int lteRssnr = -1;
    private int lteRsrp = -1;
    private int gsmBitErrorRate = -1;
    private int cdmaEcIo = -1;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		databaseReference = FirebaseDatabase.getInstance().getReference().child("DataBase");
		dataBase = new DataBase();

		signalStrengthTextView = findViewById(R.id.signalStrengthTextView);


		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			requestPermission();
			return;

		}
		initTelephonyManager();

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
					editTextMemo.setText("");
				}
			}
		});
		locationInfoTxtV = findViewById(R.id.locationInfoTxtV);
		locationProviderTxtV = findViewById(R.id.locationStrategyTxtV);
		locationProviderTxtV.setText(currentLocationProvider);
		editTextMemo = findViewById(R.id.editTextMemo);
		checkPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
				new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS);


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


						dataBase.setId((double) System.currentTimeMillis());
						dataBase.setMemo(editTextMemo.getText().toString().trim());
						dataBase.setTime(String.valueOf(location.getTime()));
						dataBase.setLatitude(location.getLatitude());
						dataBase.setLongitude(location.getLongitude());
						dataBase.setAccuracy((double) location.getAccuracy());
                        dataBase.setCdmaDbm(cdmaDbm);
                        dataBase.setLteSignalStrength(lteSignalStrength);
                        dataBase.setGsmSignalStrength(gsmSignalStrength);
                        dataBase.setLteCqi(lteCqi);
                        dataBase.setLteRssnr(lteRssnr);
                        dataBase.setLteRsrp(lteRsrp);
                        dataBase.setGsmBitErrorRate(gsmBitErrorRate);
                        dataBase.setCdmaEcIo(cdmaEcIo);



						databaseReference.child("Fused").child(editTextMemo.getText().toString()).child(String.format(String.valueOf(System.currentTimeMillis())))
								.setValue(dataBase);


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

		dataBase.setId((double) System.currentTimeMillis());
		dataBase.setMemo(editTextMemo.getText().toString().trim());
		dataBase.setTime(String.valueOf(location.getTime()));
		dataBase.setLatitude(location.getLatitude());
		dataBase.setLongitude(location.getLongitude());
		dataBase.setAccuracy((double) location.getAccuracy());
		dataBase.setCdmaDbm(cdmaDbm);
		dataBase.setLteSignalStrength(lteSignalStrength);
		dataBase.setGsmSignalStrength(gsmSignalStrength);
		dataBase.setLteCqi(lteCqi);
		dataBase.setLteRssnr(lteRssnr);
        dataBase.setLteRsrp(lteRsrp);
        dataBase.setGsmBitErrorRate(gsmBitErrorRate);
        dataBase.setCdmaEcIo(cdmaEcIo);

		if (currentLocationProvider.equals(LocationManager.NETWORK_PROVIDER)){

			databaseReference.child("Cellular").child(editTextMemo.getText().toString()).child(String.format(String.valueOf(System.currentTimeMillis())))
					.setValue(dataBase);
		}else{
			databaseReference.child("GPS").child(editTextMemo.getText().toString()).child(String.format(String.valueOf(System.currentTimeMillis())))
				.setValue(dataBase);
		}
	}

	private String locationToString(Location location) {
		Calendar locationDate = Calendar.getInstance();
		locationDate.setTimeInMillis(location.getTime());
		onMarkerUpdate(location);


		return String.format(Locale.getDefault(),
				"Lat: %.2f, Lon: %.2f, Accuracy: %.2f, time: %d (%02d.%02d.%d %02d:%02d:%02d)",
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
	@Override
	public void onMapReady(GoogleMap googleMap) {

		mMap = googleMap;

		LatLng latLng = new LatLng(52.4001, 16.9555);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
		marker = mMap.addMarker(new MarkerOptions().position(latLng).title("your current position"));
	}

	private void onMarkerUpdate(Location location) {


		if (location != null) {



			double lat = location.getLatitude();
			double lon = location.getLongitude();
			LatLng latLng = new LatLng(lat, lon);

			if (marker != null) {
				marker.remove();

				marker = mMap.addMarker(new MarkerOptions().position(latLng).title("your current position"));
			}

		}
	}


	@SuppressLint("MissingPermission")


	private void initTelephonyManager() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		NetworkListener networkListener = NetworkListener.getInstance(this);
		networkListener.addListener(this);

		telephonyManager.listen(networkListener,
				PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}





	@Override
	public void signalStrengthsChangedHandler(SignalStrength signalStrength) {

		Locale currentLocale = Locale.getDefault();


		 lteSignalStrength = getLTEparameters(signalStrength,"getLteSignalStrength"); // get signal strength
		 lteCqi = getLTEparameters(signalStrength,"getLteCqi"); // get Channel Quality Indicator value
		 lteRsrp = getLTEparameters(signalStrength,"getLteRsrp"); // Get Reference signal received power value
		 lteRssnr = getLTEparameters(signalStrength,"getLteRssnr"); // Get Reference signal Signal to Noise ratio

		String infoLte = String.format(currentLocale,"LTE info:\nSignal Strength: %d [dBm] CQI: %d RSRP: %d [dBm] RSSNR: %d\n",
				lteSignalStrength,
				lteCqi,
				lteRsrp,
				lteRssnr);

		// Get GSM related info:
		 gsmSignalStrength = signalStrength.getGsmSignalStrength(); // get GSM signal strength
		 gsmBitErrorRate = signalStrength.getGsmBitErrorRate(); // Get GSM BER

		// Create a string that holds GSM related information
		String infoGSM = String.format(currentLocale,"GSM info:\nSignal Strength: %d [dBm] BER: %d\n",
				gsmSignalStrength,
				gsmBitErrorRate);

		// Get CDMA related info:
		 cdmaDbm = signalStrength.getCdmaDbm(); // get CDMA signal strength
		 cdmaEcIo = signalStrength.getCdmaEcio();// get CDMA Ec/Io value (quality indicator)

		// Create a string that holds CDMA related information
		String infoCDMA = String.format(currentLocale,"CDMA info:\nSignal Strength: %d [dBm] Ec/Io: %d\n",
				cdmaDbm,
				cdmaEcIo);

		//Display all information in the signalStrengthTextView
		signalStrengthTextView.setText(infoLte + infoGSM + infoCDMA);
	}
	private int getLTEparameters(SignalStrength signalStrength, String parameterName)
	{
		// Valid LTE parameters:
		// - getLteAsuLevel
		// - getLteCqi
		// - getLteDbm
		// - getLteLevel
		// - getLteRsrp
		// - getLteRsrq
		// - getLteRssnr
		// - getLteSignalStrength
		if(!parameterName.contains("Lte")) {
			// Invalid parameter name
			return 0xffffff;
		}

		try
		{
			Method[] methods = android.telephony.SignalStrength.class.getMethods();

			for (Method mthd : methods)
			{
				Log.i(this.getClass().getSimpleName() +"LTE methods: ", mthd.getName());
				if (mthd.getName().equals(parameterName))
				{
					return  (int) mthd.invoke(signalStrength, new Object[]{});
				}
			}
		}
		catch (Exception e)
		{
			Log.e(this.getClass().getSimpleName(), "Exception: " + e.toString());
		}
		return 0xffffff;
	}
}
