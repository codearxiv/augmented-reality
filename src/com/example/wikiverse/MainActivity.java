package com.example.wikiverse;




import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {


	private boolean settingsChanged = true;

	private static final double RADIANS_PER_DEGREE = Math.PI/180.0;
	private static final float STANDARD_PRESSURE = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	private float refPressure = 0.0f;
	private float refAltitude = 0.0f;


	private double refLongitudeDegreesPerMeter = 0.0;
	private double refLatitudeDegreesPerMeter = 0.0;


	ArrayList<Double> locations = new ArrayList<Double>(300);
	//double[] locations = new double[300]

	private double longitude = 0.0;
	private double latitude = 0.0;
	//private float altitude = 0.0f;
	private float barAltitude = 0.0f;


	private long prevAccelerationEventTime;
	private float[] displacement = new float[3];
	private float[] velocity = new float[3];
	private float[] acceleration = new float[3];
	private float[] deviceAcceleration = new float[3];
	private float[] deviceGravity = new float[3];
	private float[] deviceGeomagnetic = new float[3];
	private float[] magneticDeviceCoor = new float[9];
	private float[] trueNorthDeviceCoor = new float[9];
	private float[] smoothedDeviceCoor = new float[9];
	private float[] declinationCoor = new float[9];



	//private final Lock velocityDataLock = new ReentrantLock();
	//DisplacementUpdate displacementUpdateThread;



	private EditText gpsLocationEditText;
	//private EditText barometerEditText;
	private EditText displacementEditText;
	//private EditText velocityEditText;
	//private EditText accelerationEditText;
	//private EditText deviceAccelerationEditText;
	//private EditText deviceGravityEditText;
	//private EditText deviceMagnetoEditText;
	private EditText deviceCoordinatesEditText;
	private EditText buildingNumEditText;
	private EditText roomNumEditText;
	private EditText locationTypeEditText;
	private EditText locationNameEditText;
	private EditText stepCountEditText;


	private CheckBox noInclinationCheckBox;
	private CheckBox distanceModeCheckBox;
	private CheckBox gyroAssistCheckBox;
	private CheckBox sensorsCheckBox;
	private CheckBox gpsCheckBox;



	private String provider;
	private Criteria criteria;
	private LocationManager locationManager;
	private PowerManager.WakeLock wakeLock;
	private boolean gpsFix = false;

	private SensorManager sensorManager;
	private boolean magnetometerSupported;
	private boolean rotationSupported;
	private boolean accelerometerSupported;
	private boolean gravitySupported;
	private boolean barometerSupported;
	private boolean gyroSupported;

	private boolean magnetometerFirstReading;
	private boolean gravityFirstReading;
	private boolean rotationFirstReading;
	private boolean gyroFirstReading;
	//----------------------------------------------------------------------------


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);



		prevAccelerationEventTime = System.nanoTime();

		displacement[0] = displacement[1] = displacement[2] = 0.0f;
		velocity[0] = velocity[1] = velocity[2] = 0.0f;
		acceleration[0] = acceleration[1] = acceleration[2] = 0.0f;
		deviceAcceleration[0] = deviceAcceleration[1] = deviceAcceleration[2] = 0.0f;
		deviceGravity[0] = deviceGravity[1] = deviceGravity[2] = 0.0f;
		deviceGeomagnetic[0] = deviceGeomagnetic[1] = deviceGeomagnetic[2] = 0.0f;

		Mtrx.setIdentity(trueNorthDeviceCoor);
		Mtrx.setIdentity(declinationCoor);


		gpsLocationEditText = (EditText) findViewById(R.id.gpsLocationEditText);
		//barometerEditText = (EditText) findViewById(R.id.barometerEditText);
		displacementEditText = (EditText) findViewById(R.id.displacementEditText);
		//velocityEditText = (EditText) findViewById(R.id.velocityEditText);
		//accelerationEditText = (EditText) findViewById(R.id.accelerationEditText);
		//deviceAccelerationEditText = (EditText) findViewById(R.id.deviceAccelerationEditText);
		//deviceGravityEditText = (EditText) findViewById(R.id.deviceGravityEditText);
		//deviceMagnetoEditText = (EditText) findViewById(R.id.deviceMagnetoEditText);
		deviceCoordinatesEditText = (EditText) findViewById(R.id.deviceCoordinatesEditText);
		buildingNumEditText = (EditText) findViewById(R.id.buildingNumEditText);
		roomNumEditText = (EditText) findViewById(R.id.roomNumEditText);
		locationTypeEditText = (EditText) findViewById(R.id.locationTypeEditText);
		locationNameEditText = (EditText) findViewById(R.id.locationNameEditText);
		stepCountEditText = (EditText) findViewById(R.id.stepCountEditText);

		stepCountEditText.setInputType(InputType.TYPE_NULL);
		gpsLocationEditText.setInputType(InputType.TYPE_NULL);
		displacementEditText.setInputType(InputType.TYPE_NULL);


		Button viewButton = (Button) findViewById(R.id.viewButton);
		viewButton.setOnClickListener(viewButtonListener);

		Button resetButton = (Button) findViewById(R.id.resetButton);
		resetButton.setOnClickListener(resetButtonListener);

		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListener);

		Button minusButton = (Button) findViewById(R.id.minusButton);
		minusButton.setOnClickListener(minusButtonListener);

		Button plusButton = (Button) findViewById(R.id.plusButton);
		plusButton.setOnClickListener(plusButtonListener);



		noInclinationCheckBox = (CheckBox) findViewById(R.id.noInclinationCheckBox);
		distanceModeCheckBox = (CheckBox) findViewById(R.id.distanceModeCheckBox);

		gyroAssistCheckBox = (CheckBox) findViewById(R.id.gyroAssistCheckBox);
		gyroAssistCheckBox.setOnClickListener(gyroAssistCheckBoxListener);

		sensorsCheckBox = (CheckBox) findViewById(R.id.sensorsCheckBox);
		sensorsCheckBox.setOnClickListener(sensorsCheckBoxListener);

		gpsCheckBox = (CheckBox) findViewById(R.id.gpsCheckBox);
		gpsCheckBox.setOnClickListener(gpsCheckBoxListener);




		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		criteria.setAltitudeRequired(false);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.addGpsStatusListener(gpsStatusListener);
		provider = locationManager.getBestProvider(criteria, true);


		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");



		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


		magnetometerSupported = !(sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD)).isEmpty();
		gravitySupported = !(sensorManager.getSensorList(Sensor.TYPE_GRAVITY)).isEmpty();
		rotationSupported = !(sensorManager.getSensorList(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)).isEmpty();
		accelerometerSupported = !(sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION)).isEmpty();
		barometerSupported = !(sensorManager.getSensorList(Sensor.TYPE_PRESSURE)).isEmpty();
		gyroSupported = !(sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR)).isEmpty();

		if(!magnetometerSupported){
			alert("Sensor Unavailable","This device does not have a magnetometer. Device orientation cannot be determined.");
		}
		if(!rotationSupported){
			alert("Sensor Unavailable","This device might not have a magnetometer or gravity sensor. Device orientation cannot be determined.");
		}
		if(!accelerometerSupported){
			alert("Sensor Unavailable","This device does not have a accelerometer. Acceleration readings will not be taken.");
		}
		if(!barometerSupported){
			alert("Sensor Unavailable","This device does not have a barometer. Pressure readings will not be taken.");
		}


	}

	//----------------------------------------------------------------------------

	@Override
	public void onRestart()
	{
		super.onRestart();

	}

	//----------------------------------------------------------------------------
	@Override
	public void onStart()
	{
		super.onStart();

		if(settingsChanged){
			SharedPreferences savedSettings = getSharedPreferences("saved_settings", MODE_PRIVATE);
			refPressure = savedSettings.getFloat("Reference Pressure",STANDARD_PRESSURE);
			refAltitude = savedSettings.getFloat("Reference Altitude",0.0f);

			//refLongitude = Double.longBitsToDouble( savedSettings.getLong("Reference Longitude",0L) );
			//refLatitude = Double.longBitsToDouble( savedSettings.getLong("Reference Latitude",0L) );
			//refDeclination = savedSettings.getFloat("Reference Declination",0.0f);
			//updateDeclination();
			//updateDegreesPerMeter();

			settingsChanged = false;
		}

		if( gpsCheckBox.isChecked() && !gpsFix ){ registerLocation(); }
		if( sensorsCheckBox.isChecked() ){ registerSensors(); }
		if( gyroAssistCheckBox.isChecked() ){ registerGyro(); }

	}
	//----------------------------------------------------------------------------
	@Override
	protected void onResume()
	{
		super.onResume();
		wakeLock.acquire();

	}
	//----------------------------------------------------------------------------
	@Override
	protected void onPause()
	{
		super.onPause();
		/*
		if( gpsCheckBox.isChecked() ){ unregisterLocation(); }
		 */
		if( sensorsCheckBox.isChecked() ){ unregisterSensors(); }
		if( gyroAssistCheckBox.isChecked() ){ unregisterGyro(); }
		wakeLock.release();
	}
	//----------------------------------------------------------------------------

	@Override
	protected void onStop()
	{
		super.onStop();

	}

	//----------------------------------------------------------------------------


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;

	}
	//----------------------------------------------------------------------------


















	//----------------------------------------------------------------------------
	private void registerSensors()
	{
		//SENSOR_DELAY_NORMAL approx. 5 samples a second
		//SENSOR_DELAY_UI approx. 15 samples a second
		//SENSOR_DELAY_GAME approx. 50 samples a second
		//SENSOR_DELAY_FASTEST approx. 60 samples a second

		magnetometerFirstReading = false;
		gravityFirstReading = false;
		rotationFirstReading = false;

		if(magnetometerSupported){
			sensorManager.registerListener(
					magnetometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
		}
		if(gravitySupported){
			sensorManager.registerListener(
					gravityListener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
		}
		if(rotationSupported){
			sensorManager.registerListener(
					rotationListener, sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
		}
		/*
		if(accelerometerSupported){
			sensorManager.registerListener(
					accelerationListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_UI);
		}*/
		if(barometerSupported){
			sensorManager.registerListener(
					barometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	//----------------------------------------------------------------------------
	private void registerGyro()
	{
		gyroFirstReading = false;

		if(gyroSupported){
			sensorManager.registerListener(
					gyroRotationListener, sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
		}

	}
	//----------------------------------------------------------------------------
	private void registerLocation()
	{
		locationManager.requestLocationUpdates(provider, 500, 0, locationListener);
		toast("Waiting for GPS signal...",Toast.LENGTH_SHORT);
		//wakeLock.acquire();
	}

	//----------------------------------------------------------------------------
	private void unregisterSensors()
	{
		if(magnetometerSupported){ sensorManager.unregisterListener(magnetometerListener); }
		if(gravitySupported){ sensorManager.unregisterListener(gravityListener); }
		if(rotationSupported){ sensorManager.unregisterListener(rotationListener); }
		if(accelerometerSupported){ sensorManager.unregisterListener(accelerationListener); }
		if(barometerSupported){ sensorManager.unregisterListener(barometerListener); }
	}

	//----------------------------------------------------------------------------
	private void unregisterGyro()
	{
		if(gyroSupported){ sensorManager.unregisterListener(gyroRotationListener); }
	}

	//----------------------------------------------------------------------------
	private void unregisterLocation()
	{
		locationManager.removeUpdates(locationListener);
		//wakeLock.release();
	}
	//----------------------------------------------------------------------------
	private void updateDeclination(float refLatitude, float refLongitude, float refAltitude)
	{

		GeomagneticField field =
				new GeomagneticField(refLatitude,refLongitude,refAltitude,System.currentTimeMillis());

		Mtrx.getRotZ( field.getDeclination()*(float)(RADIANS_PER_DEGREE), declinationCoor );

	}
	//----------------------------------------------------------------------------
	private void updateDegreesPerMeter(float refLatitude, float refLongitude)
	{
		double cosLatitude = Math.cos( Math.abs( refLatitude )*RADIANS_PER_DEGREE );
		double refEarthDiameter = (1-cosLatitude)*12713560 + cosLatitude*12756280;
		refLongitudeDegreesPerMeter = 360/( Math.PI*refEarthDiameter*cosLatitude );
		refLatitudeDegreesPerMeter = 0.00000900082;
	}
	//----------------------------------------------------------------------------
	private final double absoluteAltitude(double longitude, double latitude, double altitude)
	{
		double cosLatitude = Math.cos( Math.abs( latitude )*RADIANS_PER_DEGREE );
		return altitude + (1-cosLatitude)*12713560 + cosLatitude*12756280;

	}
	//----------------------------------------------------------------------------









	//----------------------------------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_settings:
			settingsChanged = true;
			startActivity( new Intent(MainActivity.this, SettingsActivity.class) );
			return true;
		case R.id.action_search:
			if( gpsCheckBox.isChecked() ){
				locationManager.removeUpdates(locationListener);
			}
			startActivity( new Intent(MainActivity.this, SearchActivity.class) );
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//----------------------------------------------------------------------------
	public OnClickListener viewButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent view = new Intent(MainActivity.this, ARGLActivity.class);
			if( gyroAssistCheckBox.isChecked() ){ view.putExtra("Gyro Assist",true); }
			else{ view.putExtra("Gyro Assist",false); }

			double[] temp = new double[locations.size()];
			for(int i=0;i<temp.length;i++){ temp[i] = locations.get(i); }
			view.putExtra("locations",temp);

			startActivity(view);
		}

	}; //end OnClickListener anonymous inner class


	//----------------------------------------------------------------------------
	public OnClickListener resetButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			displacement[0] = displacement[1] = displacement[2] = 0.0f;

			displacementEditText.setText(
					String.format("%.4f, %.4f, %.4f",0f,0f,0f)
					);
			stepCountEditText.setText( String.format("%d",0) );

		}

	}; //end OnClickListener anonymous inner class


	//----------------------------------------------------------------------------
	public OnClickListener saveButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			new SaveLocationTask().execute((Object[]) null);

			locations.add(longitude);
			locations.add(latitude);
			locations.add( absoluteAltitude(longitude,latitude,barAltitude) );

			//hide the soft keyboard
			//((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
			//hideSoftInputFromWindow(roomNumEditText.getWindowToken(), 0);

		}
		//----------------------------------------------------------------------------
		class SaveLocationTask extends AsyncTask<Object, Object, Object>
		{
			private SQLiteDatabase database;
			private static final String query = "CREATE TABLE locations" +
					"(_id integer primary key autoincrement," + "Building_Number STRING," +
					"Room_Number INTEGER," + "Location_Type STRING," + "Location_Name STRING," +
					"Longitude REAL," + "Latitude REAL," + "Altitude REAL);";

			@Override
			protected Cursor doInBackground(Object... params)
			{

				database = ( new DatabaseOpenHelper(MainActivity.this, "locationDB", null, 1, query) ).getWritableDatabase();

				ContentValues newLocation = new ContentValues();
				newLocation.put( "Building_Number", buildingNumEditText.getText().toString() );
				newLocation.put( "Room_Number", roomNumEditText.getText().toString() );
				newLocation.put( "Location_Type", locationTypeEditText.getText().toString() );
				newLocation.put( "Location_Name", locationNameEditText.getText().toString() );
				newLocation.put( "Longitude", longitude );
				newLocation.put( "Latitude", latitude );
				newLocation.put( "Altitude", barAltitude );

				database.insert("locations", null, newLocation);

				database.close();

				return null;

			}
		}//end class SaveDataTask
		//----------------------------------------------------------------------------

	}; //end OnClickListener anonymous inner class


	//----------------------------------------------------------------------------




	//----------------------------------------------------------------------------
	public OnClickListener minusButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(roomNumEditText.isFocused()){
				String stringNumber = roomNumEditText.getText().toString();
				int number = !stringNumber.isEmpty() ? (Integer.parseInt( stringNumber ) - 1) : 0;

				roomNumEditText.setText( String.format("%d",number) );
			}
			else if(stepCountEditText.isFocused()){

				String stringNumber = stepCountEditText.getText().toString();
				int number;

				if( !distanceModeCheckBox.isChecked() ){
					number = !stringNumber.isEmpty() ? (Integer.parseInt( stringNumber ) - 1) : -1;
					updateDisplacement(-0.5f);
				}
				else{
					number = !stringNumber.isEmpty() ? (Integer.parseInt( stringNumber ) - 3) : -3;
					updateDisplacement(-1.5f);
				}
				stepCountEditText.setText( String.format("%d",number) );
			}

		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------
	public OnClickListener plusButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(roomNumEditText.isFocused()){
				String stringNumber = roomNumEditText.getText().toString();
				int number = !stringNumber.isEmpty() ? (Integer.parseInt( stringNumber ) + 1) : 0;

				roomNumEditText.setText( String.format("%d",number) );

			}
			else if(stepCountEditText.isFocused()){
				String stringNumber = stepCountEditText.getText().toString();
				int number;

				if( !distanceModeCheckBox.isChecked() ){
					number = !stringNumber.isEmpty() ? (Integer.parseInt( stringNumber ) + 3) : 3;
					updateDisplacement(1.5f);
				}
				else{
					number = !stringNumber.isEmpty() ? (Integer.parseInt( stringNumber ) + 9) : 9;
					updateDisplacement(4.5f);
				}

				stepCountEditText.setText( String.format("%d",number) );

			}


		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------


	//----------------------------------------------------------------------------
	public OnClickListener gyroAssistCheckBoxListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if( gyroAssistCheckBox.isChecked() ){
				registerGyro();
			}
			else{
				unregisterGyro();
			}

		}

	}; //end OnClickListener anonymous inner class


	//----------------------------------------------------------------------------
	public OnClickListener sensorsCheckBoxListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if( sensorsCheckBox.isChecked() ){
				registerSensors();
			}
			else{
				unregisterSensors();
			}

		}

	}; //end OnClickListener anonymous inner class


	//----------------------------------------------------------------------------
	public OnClickListener gpsCheckBoxListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if( gpsCheckBox.isChecked() ){
				registerLocation();
			}
			else{
				unregisterLocation();
			}

		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------






























	//----------------------------------------------------------------------------

	//responds to events from the LocationManager
	private final LocationListener locationListener = new LocationListener()
	{
		private final float refUpdateBound =  0.002f;

		float refLatitude = 0.0f;
		float refLongitude = 0.0f;

		@Override
		public void onLocationChanged(Location location)
		{

			if (location != null){

				longitude = location.getLongitude();
				latitude = location.getLatitude();
				//altitude = location.getAltitude();

				if( Math.abs(longitude-refLongitude) + Math.abs(latitude-refLatitude) > refUpdateBound ){

					refLongitude = (float)longitude;
					refLatitude = (float)latitude;

					updateDeclination(refLongitude,refLatitude,0.0f);
					updateDegreesPerMeter(refLongitude,refLatitude);

				}

				gpsLocationEditText.setText(
						String.format("%.6f,  %.6f,  %.6f, %.2f",longitude, latitude, barAltitude,location.getAccuracy()) );



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
		{
			//if(status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE){}
			//else if(status == LocationProvider.AVAILABLE){}
		}

	};//end anonymous inner class




	//----------------------------------------------------------------------------
	GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener()
	{
		@Override
		public void onGpsStatusChanged(int event)
		{
			switch (event)
			{
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				gpsFix = true;
				toast("GPS signal acquired",Toast.LENGTH_SHORT);
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				gpsFix = false;
				toast("GPS stopped.",Toast.LENGTH_SHORT);
				break;
			}


		}
	}; //end anonymous inner class



	//----------------------------------------------------------------------------
	SensorEventListener magnetometerListener = new SensorEventListener()
	{
		//private final int sampleSize = 30;
		//private int sampleCount = 0;
		//private float[] accumulator = {0.0f, 0.0f, 0.0f};

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{}

		@Override
		public void onSensorChanged(SensorEvent event)
		{

			deviceGeomagnetic[0] = event.values[0];
			deviceGeomagnetic[1] = event.values[1];
			deviceGeomagnetic[2] = event.values[2];

			if( !magnetometerFirstReading ){ magnetometerFirstReading = true; }

			/*
			++sampleCount;

			accumulator[0] += event.values[0];
			accumulator[1] += event.values[1];
			accumulator[2] += event.values[2];

			deviceGeomagnetic[0] = (accumulator[0]/sampleCount);
			deviceGeomagnetic[1] = (accumulator[1]/sampleCount);
			deviceGeomagnetic[2] = (accumulator[2]/sampleCount);

			if( sampleCount >= sampleSize ){

				sampleCount = 0;
				accumulator[0] = accumulator[1] = accumulator[2] = 0.0f;
			}

			//deviceMagnetoEditText.setText(
			//		String.format("(%.4f,   %.4f,   %.4f)",deviceGeomagnetic[0], deviceGeomagnetic[1], deviceGeomagnetic[2])
			//		);
			 */

		}

	};//end anonymous inner class


	//----------------------------------------------------------------------------
	SensorEventListener gravityListener = new SensorEventListener()
	{


		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{}

		@Override
		public void onSensorChanged(SensorEvent event)
		{

			deviceGravity[0] = event.values[0];
			deviceGravity[1] = event.values[1];
			deviceGravity[2] = event.values[2];

			if( !gravityFirstReading ){ gravityFirstReading = true; }

		}

	};//end anonymous inner class

	//----------------------------------------------------------------------------
	SensorEventListener rotationListener = new SensorEventListener()
	{

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{}

		@Override
		public void onSensorChanged(SensorEvent event)
		{

			SensorManager.getRotationMatrix(magneticDeviceCoor, null, deviceGravity, deviceGeomagnetic);

			Mtrx.mult(declinationCoor,magneticDeviceCoor,trueNorthDeviceCoor);


			if( !rotationFirstReading ){
				if( magnetometerFirstReading && gravityFirstReading ){ rotationFirstReading = true; }
			}


			deviceCoordinatesEditText.setText(
					String.format(
							"(%.2f, %.2f, %.2f),(%.2f, %.2f, %.2f),(%.2f, %.2f, %.2f)",
							trueNorthDeviceCoor[0], trueNorthDeviceCoor[3], trueNorthDeviceCoor[6],
							trueNorthDeviceCoor[1], trueNorthDeviceCoor[4], trueNorthDeviceCoor[7],
							trueNorthDeviceCoor[2], trueNorthDeviceCoor[5], trueNorthDeviceCoor[8]
							)
					);



		}

	};//end anonymous inner class

	//----------------------------------------------------------------------------

	SensorEventListener gyroRotationListener = new SensorEventListener()
	{


		private float[] initialDiffMatrix = new float[9];
		private float[] gyroDeviceCoor = new float[9];
		private float[] adjustedGyroCoor = new float[9];

		private float angularDisplacementZ = 0.0f;
		private float angularVelocityZ = 0.0f;
		private final float rigidity = 0.07f;


		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			if( gyroFirstReading ){

				SensorManager.getRotationMatrixFromVector(gyroDeviceCoor,event.values);

				Mtrx.mult(initialDiffMatrix,gyroDeviceCoor,adjustedGyroCoor);


				angularDisplacementZ += angularVelocityZ;
				Mtrx.mult( Mtrx.getRotZ(angularDisplacementZ), adjustedGyroCoor, smoothedDeviceCoor);

				float diffTrueNorthAngleZ = Mtrx.getAngleZChange(smoothedDeviceCoor,trueNorthDeviceCoor);


				angularVelocityZ = (diffTrueNorthAngleZ*rigidity);



			}
			else if( rotationFirstReading ){

				gyroFirstReading = true;

				SensorManager.getRotationMatrixFromVector(gyroDeviceCoor,event.values);

				Mtrx.getRotZ( Mtrx.getAngleZChange(gyroDeviceCoor,trueNorthDeviceCoor),initialDiffMatrix );
			}


		}


	};//end anonymous inner class
	//----------------------------------------------------------------------------

	SensorEventListener accelerationListener = new SensorEventListener()
	{


		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{}

		@Override
		public void onSensorChanged(SensorEvent event)
		{



			long currentTime = System.nanoTime();
			double elapsedTimeSeconds = (currentTime - prevAccelerationEventTime)*0.000000001;

			displacement[0] += elapsedTimeSeconds*velocity[0];
			displacement[1] += elapsedTimeSeconds*velocity[1];
			displacement[2] += elapsedTimeSeconds*velocity[2];


			deviceAcceleration[0] = event.values[0];
			deviceAcceleration[1] = event.values[1];
			deviceAcceleration[2] = event.values[2];

			acceleration[0] =
					deviceAcceleration[0]*trueNorthDeviceCoor[0] +
					deviceAcceleration[1]*trueNorthDeviceCoor[3] +
					deviceAcceleration[2]*trueNorthDeviceCoor[6];
			acceleration[1] =
					deviceAcceleration[0]*trueNorthDeviceCoor[1] +
					deviceAcceleration[1]*trueNorthDeviceCoor[4] +
					deviceAcceleration[2]*trueNorthDeviceCoor[7];
			acceleration[2] =
					deviceAcceleration[0]*trueNorthDeviceCoor[2] +
					deviceAcceleration[1]*trueNorthDeviceCoor[5] +
					deviceAcceleration[2]*trueNorthDeviceCoor[8];




			velocity[0] += elapsedTimeSeconds * acceleration[0];
			velocity[1] += elapsedTimeSeconds * acceleration[1];
			velocity[2] += elapsedTimeSeconds * acceleration[2];


			prevAccelerationEventTime = currentTime;
			/*
			deviceAccelerationEditText.setText(
					String.format("%.4f,   %.4f,   %.4f",deviceAcceleration[0],deviceAcceleration[1],deviceAcceleration[2])
					);

			accelerationEditText.setText(
					String.format("%.6f,   %.6f,   %.6f",acceleration[0],acceleration[1],acceleration[2])
					);

			velocityEditText.setText(
					String.format("%.6f,   %.6f,   %.6f",velocity[0],velocity[1],velocity[2])
					);

			displacementEditText.setText(
					String.format("%.4f, %.4f, %.4f",displacement[0],displacement[1],displacement[2])
					);
			 */



		}

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

			barAltitude = refAltitude + SensorManager.getAltitude( refPressure, event.values[0] );

			//gpsLocationEditText.setText( String.format("%.6f,   %.6f,   %.6f",longitude, latitude, barAltitude) );

		}

	};//end anonymous inner class
	//----------------------------------------------------------------------------














	//----------------------------------------------------------------------------
	//southampton
	//10 meters north = 0.00009 degrees latitude
	//10 meters east = 0.000143 degrees longitude????



	void updateDisplacement(float distance)
	{


		if( noInclinationCheckBox.isChecked() ){
			double projXY[] = new double[2];
			double norm;

			norm = Math.sqrt(trueNorthDeviceCoor[1]*trueNorthDeviceCoor[1]+trueNorthDeviceCoor[4]*trueNorthDeviceCoor[4]);

			if( norm > 0.01 ){

				projXY[0] = trueNorthDeviceCoor[1]/norm;
				projXY[1] = trueNorthDeviceCoor[4]/norm;

			}
			else{
				norm = Math.sqrt(trueNorthDeviceCoor[2]*trueNorthDeviceCoor[2]+trueNorthDeviceCoor[5]*trueNorthDeviceCoor[5]);

				projXY[0] = -trueNorthDeviceCoor[2]/norm;
				projXY[1] = -trueNorthDeviceCoor[5]/norm;
			}

			double increment[] = { distance*projXY[0], distance*projXY[1] };

			displacement[0] += increment[0];
			displacement[1] += increment[1];

			longitude += increment[0]*refLongitudeDegreesPerMeter;
			latitude += increment[1]*refLatitudeDegreesPerMeter;

		}
		else{
			double increment[] = { distance*trueNorthDeviceCoor[1], distance*trueNorthDeviceCoor[4], distance*trueNorthDeviceCoor[7] };


			displacement[0] += increment[0];
			displacement[1] += increment[1];
			displacement[2] += increment[2];

			longitude += increment[0]*refLongitudeDegreesPerMeter;
			latitude += increment[1]*refLatitudeDegreesPerMeter;
		}

		displacementEditText.setText(
				String.format("%.4f, %.4f, %.4f",displacement[0],displacement[1],displacement[2])
				);

		gpsLocationEditText.setText( String.format("%.6f,   %.6f,   %.6f",longitude, latitude, barAltitude) );

	};

	//----------------------------------------------------------------------------
















	//----------------------------------------------------------------------------
	/*
	private class DisplacementUpdate extends Thread
	{
		private long prevUpdateTime;
		private long updateTimeInterval;
		private boolean isRunning = true;

		public DisplacementUpdate(long interval)
		{
			prevUpdateTime = System.nanoTime();
			updateTimeInterval = interval;
		}


		public void setRunning(boolean running)
		{
			isRunning = running;
		}

		@Override
		public void run()
		{

			while(isRunning){



				velocityDataLock.lock();
				//----------------------
				long elapsedTime = System.nanoTime() - prevUpdateTime;

				displacement[0] += elapsedTime*velocity[0];
				displacement[1] += elapsedTime*velocity[1];
				displacement[2] += elapsedTime*velocity[2];

				//----------------------
				velocityDataLock.unlock();

				prevUpdateTime += elapsedTime;


				try{ Thread.sleep(updateTimeInterval); }
				catch(InterruptedException e){}

			}

		}


	};//end Runnable

	 */

	//----------------------------------------------------------------------------








	//----------------------------------------------------------------------------
	void alert(String title, String alert)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);

		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(alert);
		dialogBuilder.setPositiveButton("OK", null);
		dialogBuilder.show();

	}
	//----------------------------------------------------------------------------
	void toast(String message, int duration)
	{
		Toast results = Toast.makeText(MainActivity.this, message, duration);

		//center the Toast in the screen
		results.setGravity(Gravity.CENTER, results.getXOffset()/2, results.getYOffset()/2);
		results.show();

	}
	//----------------------------------------------------------------------------



}//end main class
