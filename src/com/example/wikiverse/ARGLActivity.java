package com.example.wikiverse;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;


public class ARGLActivity extends Activity {

	private ARGLSurfaceView glView;

	private static final double RADIANS_PER_DEGREE = Math.PI/180.0;
	private static final float STANDARD_PRESSURE = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	private float refPressure = 0.0f;
	private float refAltitude = 0.0f;
	private float barAltitude = 0.0f;

	private float[] position = new float[3];

	private float[] locations = null;


	private float[] deviceGravity = new float[3];
	private float[] deviceGeomagnetic = new float[3];
	private float[] magneticDeviceCoor = new float[9];
	private float[] trueNorthDeviceCoor = new float[9];
	private float[] smoothedDeviceCoor = new float[9];
	private float[] declinationCoor = new float[9];

	private LocationManager locationManager;
	private SensorManager sensorManager;
	private PowerManager.WakeLock wakeLock;

	private boolean magnetometerSupported;
	private boolean gravitySupported;
	private boolean rotationSupported;
	private boolean gyroSupported;
	private boolean barometerSupported;

	private boolean gyroAssist = false;


	private boolean magnetometerFirstReading = false;
	private boolean gravityFirstReading = false;
	private boolean rotationFirstReading = false;
	private boolean gyroFirstReading = false;




	//----------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		glView = new ARGLSurfaceView(this);
		setContentView(glView);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		Bundle extras = getIntent().getExtras();
		if( extras!=null ){
			gyroAssist = extras.getBoolean("Gyro Assist",false);

			refPressure = extras.getFloat("Reference Pressure",STANDARD_PRESSURE);
			refAltitude = extras.getFloat("Reference Altitude",0.0f);

			//float refDeclination = extras.getFloat("Reference Declination",0.0f);
			float refLongitude = extras.getFloat("Reference Longitude",0.0f);
			float refLatitude = extras.getFloat("Reference Latitude",0.0f);
			updateDeclination(refLongitude,refLatitude,0.0f);
		}

		position[0] = position[1] = position[2] = 0.0f;
		deviceGravity[0] = deviceGravity[1] = deviceGravity[2] = 0.0f;
		deviceGeomagnetic[0] = deviceGeomagnetic[1] = deviceGeomagnetic[2] = 0.0f;

		Mtrx.setIdentity(trueNorthDeviceCoor);
		Mtrx.setIdentity(magneticDeviceCoor);
		if(gyroAssist){ Mtrx.setIdentity(smoothedDeviceCoor); }


		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");


		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

		magnetometerSupported = !(sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD)).isEmpty();
		gravitySupported = !(sensorManager.getSensorList(Sensor.TYPE_GRAVITY)).isEmpty();
		rotationSupported = !(sensorManager.getSensorList(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)).isEmpty();
		gyroSupported = !(sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR)).isEmpty();
		barometerSupported = !(sensorManager.getSensorList(Sensor.TYPE_PRESSURE)).isEmpty();


	}

	//----------------------------------------------------------------------------

	@Override
	public void onRestart()
	{
		super.onRestart();

	}
	//----------------------------------------------------------------------------
	@Override
	protected void onPause() {

		super.onPause();
		glView.onPause();

		unregisterLocation();
		unregisterSensors();
		wakeLock.release();

	}
	//----------------------------------------------------------------------------
	@Override
	protected void onResume() {

		super.onResume();
		glView.onResume();

		//if(locationsInitialized){ registerSensors(); }
		//else{ registerBarometer(); }
		//initializeLocations(0,0,0);
		//registerSensors();
		//registerBarometer();
		registerLocation();


		wakeLock.acquire();
	}
	//----------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.argl, menu);
		return true;
	}
	//----------------------------------------------------------------------------






	//----------------------------------------------------------------------------
	private void registerLocation()
	{
		locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,500,0,locationListener);
	}
	//----------------------------------------------------------------------------
	void registerSensors()
	{

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
					rotationListener, sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
		}
		if(gyroSupported && gyroAssist){
			sensorManager.registerListener(
					gyroRotationListener, sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
		}
		if(barometerSupported){
			sensorManager.registerListener(
					barometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
		}

	}
	//----------------------------------------------------------------------------
	void registerBarometer()
	{
		if(barometerSupported){
			sensorManager.registerListener(
					barometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	//----------------------------------------------------------------------------
	private void unregisterLocation()
	{
		locationManager.removeUpdates(locationListener);


	}
	//----------------------------------------------------------------------------
	void unregisterSensors()
	{
		if(magnetometerSupported){ sensorManager.unregisterListener(magnetometerListener); }
		if(gravitySupported){ sensorManager.unregisterListener(gravityListener); }
		if(rotationSupported){ sensorManager.unregisterListener(rotationListener); }
		if(gyroSupported && gyroAssist){ sensorManager.unregisterListener(rotationListener); }

		magnetometerFirstReading = false;
		gravityFirstReading = false;
		rotationFirstReading = false;
		gyroFirstReading = false;
	}

	//----------------------------------------------------------------------------

	private void updateDeclination(float refLatitude, float refLongitude, float refAltitude)
	{

		GeomagneticField field = new GeomagneticField(refLatitude,refLongitude,refAltitude,System.currentTimeMillis());

		Mtrx.getRotZ( field.getDeclination()*(float)(RADIANS_PER_DEGREE), declinationCoor );

	}

	//----------------------------------------------------------------------------
	private final void degreesToEuclidean(double longitude, double latitude, double absoluteAltitude,
			double originLongitude, double originLatitude, double originAbsoluteAltitude, float[] pos, int index)
	{


		double cosLatitudeDiff = Math.cos( ( originLatitude - latitude )*RADIANS_PER_DEGREE );
		double sinLatitudeDiff = Math.sin( ( originLatitude - latitude )*RADIANS_PER_DEGREE );
		double cosLongitudeDiff = Math.cos( ( longitude - originLongitude )*RADIANS_PER_DEGREE );
		double sinLongitudeDiff = Math.sin( ( longitude - originLongitude )*RADIANS_PER_DEGREE );


		pos[index] = (float)(-sinLongitudeDiff*cosLatitudeDiff*absoluteAltitude);
		pos[index+1] = (float)(sinLatitudeDiff*absoluteAltitude);
		//pos[index+2] = (float)( (cosLongitudeDiff*cosLatitudeDiff*absoluteAltitude) - originAbsoluteAltitude );
		pos[index+2] = (float)( absoluteAltitude - originAbsoluteAltitude );

		//count++;

		//if(count>15){
		//toast(String.valueOf(pos[index])+","+String.valueOf(pos[index+1])+","+String.valueOf(pos[index+2]),0);
		//toast(String.valueOf(barAltitude)+","+String.valueOf(pos[index+2]),0);
		//toast(String.valueOf(absoluteAltitude)+","+String.valueOf(originAbsoluteAltitude),0);
		//toast(String.valueOf(absoluteAltitude),0);
		//count = 0;
		//}
	}

	//----------------------------------------------------------------------------
	private final double absoluteAltitude(double longitude, double latitude, double altitude)
	{
		double cosLatitude = Math.cos( Math.abs( latitude )*RADIANS_PER_DEGREE );
		return altitude + (1-cosLatitude)*12713560 + cosLatitude*12756280;

	}
	//----------------------------------------------------------------------------
	void initializeLocations(double originLongitude, double originLatitude, double originAbsoluteAltitude)
	{


		Bundle extras = getIntent().getExtras();

		if( extras!=null ){

			double[] locationsDegrees = extras.getDoubleArray("locations");

			if(locations == null){ locations = new float[locationsDegrees.length]; }
			//locations = new float[99];
			for(int i=2;i<locations.length;i+=3){
				//locations[i-2]=i; locations[i]=i; locations[i-1]=i+5;
				degreesToEuclidean(locationsDegrees[i-2],locationsDegrees[i-1],locationsDegrees[i],
						originLongitude,originLatitude,originAbsoluteAltitude,locations,i-2);
			}

		}


	}

	//----------------------------------------------------------------------------

	//----------------------------------------------------------------------------
	void toast(String message, int duration)
	{
		Toast results = Toast.makeText(ARGLActivity.this, message, duration);

		//center the Toast in the screen
		results.setGravity(Gravity.CENTER, results.getXOffset()/2, results.getYOffset()/2);
		results.show();

	}
	//----------------------------------------------------------------------------












	//----------------------------------------------------------------------------
	//responds to events from the LocationManager
	private final LocationListener locationListener = new LocationListener()
	{
		private final float refUpdateBound =  0.002f;
		private final float accuracyUpdateFactor =  0.75f;
		private final float targetAccuracy =  2.0f;
		private float previousAccuracy =  100.0f;

		private boolean firstGoodFix = false;
		private boolean firstFix = false;

		private double originLongitude = 0.0f;
		private double originLatitude = 0.0f;
		private double originAbsoluteAltitude = 0.0f;

		private double refLongitude = 0.0f;
		private double refLatitude = 0.0f;

		@Override
		public void onLocationChanged(Location location)
		{

			if (location != null){

				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double absoluteAltitude = absoluteAltitude(longitude,latitude,barAltitude);


				if( Math.abs(longitude-refLongitude) + Math.abs(latitude-refLatitude) > refUpdateBound ){

					refLongitude = longitude;
					refLatitude = latitude;

					updateDeclination((float)refLongitude,(float)refLatitude,0.0f);

				}

				if( !firstGoodFix ){

					float accuracy = location.getAccuracy();

					//if(count == 7){ toast(String.valueOf(accuracy),0); }

					if( accuracy<=previousAccuracy*accuracyUpdateFactor ){
						originLongitude = longitude;
						originLatitude = latitude;
						originAbsoluteAltitude = absoluteAltitude - barAltitude;

						initializeLocations(originLongitude,originLatitude,originAbsoluteAltitude);

						previousAccuracy = accuracy;
					}

					if( accuracy<=targetAccuracy ){ firstGoodFix = true; }

					if( !firstFix ){
						registerSensors();
						firstFix = true;
					}


				}


				degreesToEuclidean(longitude,latitude,absoluteAltitude,
						originLongitude,originLatitude,originAbsoluteAltitude,position,0);

				//toast(String.valueOf(position[0])+","+String.valueOf(position[1])+","+String.valueOf(position[2]),0);

			}

		}

		@Override
		public void onProviderDisabled(String provider)
		{
			firstGoodFix = false;
			firstFix = false;
		}

		@Override
		public void onProviderEnabled(String provider)
		{}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{}

	};//end anonymous inner class

	//----------------------------------------------------------------------------

	SensorEventListener magnetometerListener = new SensorEventListener()
	{

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


			if( !gyroAssist ){ glView.render(); }

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


				glView.render();

			}
			else if( rotationFirstReading ){

				gyroFirstReading = true;

				SensorManager.getRotationMatrixFromVector(gyroDeviceCoor,event.values);

				Mtrx.getRotZ( Mtrx.getAngleZChange(gyroDeviceCoor,trueNorthDeviceCoor),initialDiffMatrix );
			}


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

		}

	};//end anonymous inner class
	//----------------------------------------------------------------------------






















	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------
	public class ARGLSurfaceView extends GLSurfaceView
	{

		private final ARGLRenderer renderer;

		//----------------------------------------------------------------------------
		public ARGLSurfaceView(Context context)
		{
			super(context);

			renderer = new ARGLRenderer();
			setRenderer(renderer);

			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}

		//----------------------------------------------------------------------------
		public void render()
		{

			if( gyroAssist ){

				renderer.setCamera(
						position[0], position[2], position[1],
						smoothedDeviceCoor[2], -smoothedDeviceCoor[8], -smoothedDeviceCoor[5],
						-smoothedDeviceCoor[1], smoothedDeviceCoor[7], smoothedDeviceCoor[4]
						);

			}
			else{

				renderer.setCamera(
						position[0], position[2], position[1],
						trueNorthDeviceCoor[2], -trueNorthDeviceCoor[8], -trueNorthDeviceCoor[5],
						-trueNorthDeviceCoor[1], trueNorthDeviceCoor[7], trueNorthDeviceCoor[4]
						);
			}


			requestRender();
		}
		//----------------------------------------------------------------------------

		@Override
		public boolean onTouchEvent(MotionEvent e)
		{
			return true;
		}
		//----------------------------------------------------------------------------

		//----------------------------------------------------------------------------
		public class ARGLRenderer extends GLRenderer
		{


			private BasicShapeVertexOrder shape, shape2, shape3;
			private Pyramid pyramid;
			private Triangle triangle;
			private Line line;

			ARGLRenderer() {
				super();
			}


			@Override
			public void onSurfaceCreated(GL10 gl, EGLConfig config)
			{
				super.onSurfaceCreated(gl, config);

				gl.glEnable(gl.GL_CULL_FACE);         // do not calculate inside of polys
				gl.glFrontFace(gl.GL_CW);            // counter clock-wise polygons are out

				gl.glLineWidth(3);
				gl.glColor4f(0.63671875f, 0.76953125f, 0.22265625f, 0f);


				line = new Line((GL11)gl,0,-10,0,0,10,0);
				line.mapToBuffer((GL11)gl);

				pyramid = new Pyramid((GL11)gl);
				pyramid.mapToBuffer((GL11)gl);

				//triangle = new Triangle((GL11)gl);
				//triangle.mapToBuffer((GL11)gl);


				/*
				int N = 170;

				shape3 = new BasicShapeVertexOrder((GL11)gl,GL10.GL_TRIANGLES, 3*N);
				shape2 = new BasicShapeVertexOrder((GL11)gl,GL10.GL_TRIANGLES, 3*N*N);
				shape = new BasicShapeVertexOrder((GL11)gl,GL10.GL_TRIANGLES, 3*N*N*N);

				for(int i=0;i<N;i++){ shape3.put(triangle, 8*i,0,0); }
				for(int i=0;i<N;i++){ shape2.put(shape3, 0,8*i,0); }
				for(int i=0;i<N;i++){ shape.put(shape2, 0,0,8*i); }

				shape.mapToBuffer((GL11)gl);

				gl.glDisable(gl.GL_LIGHTING);

				shape.bindToBuffer((GL11)gl);
				gl.glPushMatrix();
				gl.glRotatef(angle, 0, 1, 0); angle+=0.7f;
				shape.draw((GL11)gl);
				gl.glPopMatrix();
				 */
			}
			float angle = 0.0f;
			@Override
			public void onDrawFrame(GL10 gl)
			{

				super.onDrawFrame(gl);


				if(locations!=null){

					gl.glEnable(gl.GL_LIGHTING);
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
					gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
					gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

					pyramid.bindToBuffer((GL11)gl);
					angle+=5f;
					for(int i=2; i<locations.length; i+=3){

						gl.glPushMatrix();

						//gl.glTranslatef(locations[i-2],locations[i],locations[i-1]);
						gl.glTranslatef(locations[i-2],0,locations[i-1]);

						gl.glRotatef(angle, 0, 1, 0);

						pyramid.draw((GL11)gl);

						gl.glPopMatrix();
					}

					gl.glDisable(gl.GL_LIGHTING);
					gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
					gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

					line.bindToBuffer((GL11)gl);

					for(int i=2; i<locations.length; i+=3){

						gl.glPushMatrix();

						//gl.glTranslatef(locations[i-2],locations[i],locations[i-1]);
						gl.glTranslatef(locations[i-2],0,locations[i-1]);


						line.draw((GL11)gl);

						gl.glPopMatrix();
					}

					gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);


				}


			}

		}
		//----------------------------------------------------------------------------

	}


	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------



}
