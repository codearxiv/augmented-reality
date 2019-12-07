package com.example.wikiverse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity
{
	private EditText seaPressureEditText;
	private EditText refPressureEditText;
	private EditText refAltitudeEditText;
	private EditText refDeclinationEditText;
	private EditText refLocationEditText;
	private EditText timeEditText;

	private SharedPreferences savedSettings;
	private static final float STANDARD_PRESSURE = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;


	private LocationManager locationManager;
	private SensorManager sensorManager;


	//----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);


		Button getTimeButton = (Button) findViewById(R.id.getTimeButton);
		getTimeButton.setOnClickListener(getTimeButtonListener);

		Button getRefLocationButton = (Button) findViewById(R.id.getRefLocationButton);
		getRefLocationButton.setOnClickListener(getRefLocationButtonListener);

		Button standardPressureButton = (Button) findViewById(R.id.standardPressureButton);
		standardPressureButton.setOnClickListener(standardPressureButtonListener);

		Button getRefPressureButton = (Button) findViewById(R.id.getRefPressureButton);
		getRefPressureButton.setOnClickListener(getRefPressureButtonListener);

		Button getRefAltitudeButton = (Button) findViewById(R.id.getRefAltitudeButton);
		getRefAltitudeButton.setOnClickListener(getRefAltitudeButtonListener);

		Button getRefDeclinationButton = (Button) findViewById(R.id.getRefDeclinationButton);
		getRefDeclinationButton.setOnClickListener(getRefDeclinationButtonListener);

		Button saveChangesButton = (Button) findViewById(R.id.saveChangesButton);
		saveChangesButton.setOnClickListener(saveChangesButtonListener);

		timeEditText = (EditText) findViewById(R.id.timeEditText);
		refLocationEditText = (EditText) findViewById(R.id.refLocationEditText);
		seaPressureEditText = (EditText) findViewById(R.id.seaPressureEditText);
		refPressureEditText = (EditText) findViewById(R.id.refPressureEditText);
		refAltitudeEditText = (EditText) findViewById(R.id.refAltitudeEditText);
		refDeclinationEditText = (EditText) findViewById(R.id.refDeclinationEditText);

		savedSettings = getSharedPreferences("saved_settings", MODE_PRIVATE);

		long time = savedSettings.getLong("Time",0L);
		double refLongitude = Double.longBitsToDouble( savedSettings.getLong("Reference Longitude",0L) );
		double refLatitude = Double.longBitsToDouble( savedSettings.getLong("Reference Latitude",0L) );
		float seaPressure = savedSettings.getFloat("Sea Pressure",STANDARD_PRESSURE);
		float refPressure = savedSettings.getFloat("Reference Pressure",STANDARD_PRESSURE);
		float refAltitude = savedSettings.getFloat("Reference Altitude",0.0f);
		float refDeclination = savedSettings.getFloat("Reference Declination",0.0f);

		timeEditText.setText( String.format("%d",time) );
		refLocationEditText.setText( String.format("%.6f, %.6f",refLongitude, refLatitude) );
		seaPressureEditText.setText( String.format("%.6f",seaPressure) );
		refPressureEditText.setText( String.format("%.6f",refPressure) );
		refAltitudeEditText.setText( String.format("%.6f",refAltitude) );
		refDeclinationEditText.setText( String.format("%.06f",refDeclination) );

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

	}

	//----------------------------------------------------------------------------
	@Override
	public void onRestart()
	{
		super.onRestart();

	}
	//----------------------------------------------------------------------------
	// called when Activity becoming visible to the user
	@Override
	public void onStart()
	{
		super.onStart();

	}
	//----------------------------------------------------------------------------
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	//----------------------------------------------------------------------------
	@Override
	protected void onPause()
	{
		super.onPause();

		sensorManager.unregisterListener(barometerListener);
		locationManager.removeUpdates(locationListener);

	}
	//----------------------------------------------------------------------------

	@Override
	protected void onStop()
	{

		super.onStop();

	}
	//----------------------------------------------------------------------------







	//----------------------------------------------------------------------------

	public OnClickListener getTimeButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			timeEditText.setText( String.format("%d",System.currentTimeMillis()) );

		}
	}; //end OnClickListener anonymous inner class
	//----------------------------------------------------------------------------

	public OnClickListener getRefLocationButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,500, 0, locationListener);

		}
	}; //end OnClickListener anonymous inner class
	//----------------------------------------------------------------------------

	public OnClickListener standardPressureButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			seaPressureEditText.setText( String.format("%.6f",STANDARD_PRESSURE) );
		}
	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------

	public OnClickListener getRefPressureButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			boolean barometerSupported = sensorManager.registerListener(
					barometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);

			if(!barometerSupported)
			{
				alert("Sensor Unavailable","This device does not have a barometer.");
			}


		}
	}; //end OnClickListener anonymous inner class
	//----------------------------------------------------------------------------
	public OnClickListener getRefAltitudeButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			//refAltitudeEditText.requestFocus();
			float seaPressure = Float.parseFloat( seaPressureEditText.getText().toString() );
			float refPressure = Float.parseFloat( refPressureEditText.getText().toString() );
			float refAltitude = SensorManager.getAltitude(seaPressure, refPressure);
			refAltitudeEditText.setText( String.format("%.6f",refAltitude) );

		}
	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------
	public OnClickListener getRefDeclinationButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			double[] refLongitude = {0.0};
			double[] refLatitude = {0.0};

			parseLocationText(refLongitude,refLatitude);


			long time = Long.parseLong( timeEditText.getText().toString() );
			float refAltitude = Float.parseFloat( refAltitudeEditText.getText().toString() );

			GeomagneticField field = new GeomagneticField((float)refLatitude[0],(float)refLongitude[0],refAltitude,time);

			refDeclinationEditText.setText( String.format("%.6f",field.getDeclination()) );

		}
	}; //end OnClickListener anonymous inner class
	//----------------------------------------------------------------------------
	public OnClickListener saveChangesButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			double[] refLongitude = {0.0};
			double[] refLatitude = {0.0};

			parseLocationText(refLongitude,refLatitude);

			long time = Long.parseLong( timeEditText.getText().toString() );
			float seaPressure = Float.parseFloat( seaPressureEditText.getText().toString() );
			float refPressure = Float.parseFloat( refPressureEditText.getText().toString() );
			float refAltitude = Float.parseFloat( refAltitudeEditText.getText().toString() );
			float refDeclination = Float.parseFloat( refDeclinationEditText.getText().toString() );


			SharedPreferences.Editor preferencesEditor = savedSettings.edit();

			preferencesEditor.putLong( "Reference Longitude", Double.doubleToLongBits(refLongitude[0]) );
			preferencesEditor.putLong( "Reference Latitude", Double.doubleToLongBits(refLatitude[0]) );
			preferencesEditor.putLong("Time", time);
			preferencesEditor.putFloat("Sea Pressure", seaPressure);
			preferencesEditor.putFloat("Reference Pressure", refPressure);
			preferencesEditor.putFloat("Reference Altitude", refAltitude);
			preferencesEditor.putFloat("Reference Declination", refDeclination);
			preferencesEditor.apply();
			toast("Settings saved.",Toast.LENGTH_SHORT);

		}
	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------















	//----------------------------------------------------------------------------
	//responds to events from the LocationManager
	private final LocationListener locationListener = new LocationListener()
	{

		@Override
		public void onLocationChanged(Location location)
		{
			//gpsFix = true;
			if (location != null){

				double refLongitude = location.getLongitude();
				double refLatitude = location.getLatitude();

				refLocationEditText.setText( String.format("%.7f, %.7f",refLongitude, refLatitude) );

				locationManager.removeUpdates(locationListener);
			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{}

		@Override
		public void onProviderEnabled(String provider)
		{}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{}

	};//end anonymous inner class


	//----------------------------------------------------------------------------
	SensorEventListener barometerListener = new SensorEventListener()
	{
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			refPressureEditText.setText( String.format("%.6f",event.values[0]) );
			sensorManager.unregisterListener(barometerListener);

		}

	};//end anonymous inner class
	//----------------------------------------------------------------------------








	//----------------------------------------------------------------------------
	private void parseLocationText(double[] longitude, double[] latitude)
	{
		String locationString = refLocationEditText.getText().toString();
		StringBuilder longitudeString = new StringBuilder();
		StringBuilder latitudeString = new StringBuilder();


		boolean endOfFirstWord = false;


		for(int i=0; i<locationString.length(); i++){

			if( !endOfFirstWord ){

				if( locationString.charAt(i) == ','){
					endOfFirstWord = true;
				}
				else if( locationString.charAt(i) != ' ' ){
					longitudeString.append(locationString.charAt(i));
				}


			}
			else{
				if( locationString.charAt(i) == ','){
					break;
				}
				else if( locationString.charAt(i) != ' ' ){
					latitudeString.append(locationString.charAt(i));
				}

			}
		}

		longitude[0] = longitudeString.length()==0 ? 0.0 : Double.parseDouble( longitudeString.toString() );
		latitude[0] = latitudeString.length()==0 ? 0.0 : Double.parseDouble( latitudeString.toString() );

	}
	//----------------------------------------------------------------------------
	void alert(String title, String alert)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);

		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(alert);
		dialogBuilder.setPositiveButton("OK", null);
		dialogBuilder.show();

	}
	//----------------------------------------------------------------------------
	void toast(String message, int duration)
	{
		Toast results = Toast.makeText(SettingsActivity.this, message, duration);

		//center the Toast in the screen
		results.setGravity(Gravity.CENTER, results.getXOffset()/2, results.getYOffset()/2);
		results.show();

	}
	//----------------------------------------------------------------------------
}
