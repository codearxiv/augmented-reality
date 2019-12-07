package com.example.wikiverse;

import android.hardware.SensorManager;

public class Mtrx{





	//----------------------------------------------------------------------------
	public static void mult(float[] a, float[] b, float[] c)
	{

		c[0] = a[0]*b[0] + a[1]*b[3] + a[2]*b[6];
		c[1] = a[0]*b[1] + a[1]*b[4] + a[2]*b[7];
		c[2] = a[0]*b[2] + a[1]*b[5] + a[2]*b[8];

		c[3] = a[3]*b[0] + a[4]*b[3] + a[5]*b[6];
		c[4] = a[3]*b[1] + a[4]*b[4] + a[5]*b[7];
		c[5] = a[3]*b[2] + a[4]*b[5] + a[5]*b[8];

		c[6] = a[6]*b[0] + a[7]*b[3] + a[8]*b[6];
		c[7] = a[6]*b[1] + a[7]*b[4] + a[8]*b[7];
		c[8] = a[6]*b[2] + a[7]*b[5] + a[8]*b[8];

	}

	//----------------------------------------------------------------------------
	public static void assign(float[] a,float[] c)
	{
		//System.arraycopy(c, 0, a, 0, 8);

		c[1] = a[1];
		c[2] = a[2];

		c[3] = a[3];
		c[4] = a[4];
		c[5] = a[5];

		c[6] = a[6];
		c[7] = a[7];
		c[8] = a[8];

	}

	//----------------------------------------------------------------------------
	public static void setIdentity(float[] c)
	{

		c[0] = 1.0f;
		c[1] = 0.0f;
		c[2] = 0.0f;

		c[3] = 0.0f;
		c[4] = 1.0f;
		c[5] = 0.0f;

		c[6] = 0.0f;
		c[7] = 0.0f;
		c[8] = 1.0f;

	}
	//----------------------------------------------------------------------------
	public static void getRotX(float angle, float[] c)
	{

		float cosAngle = (float)Math.cos(angle);
		float sinAngle = (float)Math.sin(angle);

		c[0] = 1.0f;
		c[3] = 0.0f;
		c[6] = 0.0f;
		c[1] = 0.0f;
		c[4] = cosAngle;
		c[7] = -sinAngle;
		c[2] = 0.0f;
		c[5] = sinAngle;
		c[8] = cosAngle;


	}
	//----------------------------------------------------------------------------
	public static void getRotY(float angle, float[] c)
	{

		float cosAngle = (float)Math.cos(angle);
		float sinAngle = (float)Math.sin(angle);

		c[0] = cosAngle;
		c[3] = 0.0f;
		c[6] = sinAngle;
		c[1] = 0.0f;
		c[4] = 1.0f;
		c[7] = 0.0f;
		c[2] = -sinAngle;
		c[5] = 0.0f;
		c[8] = cosAngle;

	}
	//----------------------------------------------------------------------------
	public static void getRotZ(float angle, float[] c)
	{

		float cosAngle = (float)Math.cos(angle);
		float sinAngle = (float)Math.sin(angle);

		c[0] = cosAngle;
		c[3] = -sinAngle;
		c[6] = 0.0f;
		c[1] = sinAngle;
		c[4] = cosAngle;
		c[7] = 0.0f;
		c[2] = 0.0f;
		c[5] = 0.0f;
		c[8] = 1.0f;

	}
	//----------------------------------------------------------------------------
	public static void getRotYX(float angleX, float angleY, float[] c)
	{

		float cosAngleX = (float)Math.cos(angleX);
		float sinAngleX = (float)Math.sin(angleX);
		float cosAngleY = (float)Math.cos(angleY);
		float sinAngleY = (float)Math.sin(angleY);

		c[0] = cosAngleY;
		c[3] = 0.0f;
		c[6] = sinAngleY;
		c[1] = sinAngleY*sinAngleX;
		c[4] = cosAngleX;
		c[7] = -cosAngleY*sinAngleX;
		c[2] = -sinAngleY*cosAngleX;
		c[5] = sinAngleX;
		c[8] = cosAngleY*cosAngleX;

	}
	//----------------------------------------------------------------------------
	public static void getRotZXY(float[] angle, float[] c)
	{

		float cz = (float)Math.cos(angle[0]);
		float sz = (float)Math.sin(angle[0]);

		float cx = (float)Math.cos(angle[1]);
		float sx = (float)Math.sin(angle[1]);

		float cy = (float)Math.cos(angle[2]);
		float sy = (float)Math.sin(angle[2]);

		c[0] = cz*cy - sz*sx*sy;
		c[3] = sz*cy + cz*sx*sy;
		c[6] = -cx*sy;
		c[1] = -sz*cx;
		c[4] = cz*cx;
		c[7] = sx;
		c[2] = cz*sy + sz*sx*cy;
		c[5] = sz*sy - cz*sx*cy;
		c[8] = cx*cy;

	}

	//----------------------------------------------------------------------------
	public static void getRotYXZ(float[] angle, float[] c)
	{

		float cz = (float)Math.cos(angle[0]);
		float sz = (float)Math.sin(angle[0]);

		float cx = (float)Math.cos(angle[1]);
		float sx = (float)Math.sin(angle[1]);

		float cy = (float)Math.cos(angle[2]);
		float sy = (float)Math.sin(angle[2]);

		c[0] = cy*cz + sy*sx*sz;
		c[3] = cx*sz;
		c[6] = -sy*cz + cy*sx*sz;
		c[1] = -cy*sz + sy*sx*sz;
		c[4] = cx*cz;
		c[7] = sy*sz + cy*sx*cz;
		c[2] = sy*cx;
		c[5] = -sx;
		c[8] = cy*cx;

	}

	//----------------------------------------------------------------------------
	public static void getRotAA(float angle, float[] v, float[] c)
	{

		float cosAngle = (float)Math.cos(angle);
		float sinAngle = (float)Math.sin(angle);

		float t = 1.0f-cosAngle;

		float tx = t*v[0];
		float ty = t*v[1];
		float tz = t*v[2];

		float sx = sinAngle*v[0];
		float sy = sinAngle*v[1];
		float sz = sinAngle*v[2];

		c[0] = (tx*v[0] + cosAngle);
		c[3] = (tx*v[1] + sz);
		c[6] = (tx*v[2] - sy);
		c[1] = (ty*v[0] - sz);
		c[4] = (ty*v[1] + cosAngle);
		c[7] = (ty*v[2] + sx);
		c[2] = (tz*v[0] + sy);
		c[5] = (tz*v[1] - sx);
		c[8] = (tz*v[2] + cosAngle);


	}
	//----------------------------------------------------------------------------
	public static float getAngleZChange(float[] a, float[] b)
	{
		float angle = 0.0f;
		float cosAngle = 0.0f;
		float sign = 1.0f;

		if( Math.abs(a[8])<0.8f && Math.abs(b[8])<0.8f ){

			cosAngle = ( a[2]*b[2] + a[5]*b[5] )/( (float)Math.sqrt( (a[2]*a[2]+a[5]*a[5])*(b[2]*b[2]+b[5]*b[5]) ) );
			if( a[2]*b[5]-a[5]*b[2] > 0 ){ sign = -1.0f; }

		}
		else if( Math.abs(a[6])<0.8f && Math.abs(b[6])<0.8f ){

			cosAngle = ( a[0]*b[0] + a[3]*b[3] )/( (float)Math.sqrt( (a[0]*a[0]+a[3]*a[3])*(b[0]*b[0]+b[3]*b[3]) ) );
			if( a[0]*b[3]-a[3]*b[0] > 0 ){ sign = -1.0f; }
		}
		else if( Math.abs(a[7])<0.8f && Math.abs(b[7])<0.8f ){

			cosAngle = ( a[1]*b[1] + a[4]*b[4] )/( (float)Math.sqrt( (a[1]*a[1]+a[4]*a[4])*(b[1]*b[1]+b[4]*b[4]) ) );
			if( a[1]*b[4]-a[4]*b[1] > 0 ){ sign = -1.0f; }
		}

		if( cosAngle>=1.0f ){ angle = 0.0f; }
		else if( cosAngle<=-1.0f ){ angle = (float)Math.PI; }
		else{ angle = sign*(float)Math.acos( cosAngle ); }

		return angle;
	}
	//----------------------------------------------------------------------------
	public static void getAngleYXZChange(float[] a, float[] b, float[] angle)
	{
		SensorManager.getAngleChange(angle,b,a);
		angle[0] = -angle[0];
		angle[1] = -angle[1];

	}
	//----------------------------------------------------------------------------



	//----------------------------------------------------------------------------
	public static float[] mult(float[] a, float[] b)
	{

		float[] c = new float[9];

		mult(a,b,c);

		return c;
	}
	//----------------------------------------------------------------------------
	public static float[] getIdentity()
	{
		float[] c = new float[9];

		setIdentity(c);

		return c;
	}
	//----------------------------------------------------------------------------
	public static float[] getRotX(float angle)
	{
		float[] c = new float[9];

		getRotX(angle,c);

		return c;
	}
	//----------------------------------------------------------------------------
	public static float[] getRotY(float angle)
	{
		float[] c = new float[9];

		getRotY(angle,c);

		return c;

	}
	//----------------------------------------------------------------------------
	public static float[] getRotZ(float angle)
	{
		float[] c = new float[9];

		getRotZ(angle,c);

		return c;

	}
	//----------------------------------------------------------------------------
	public static float[] getRotYX(float angleX, float angleY)
	{
		float[] c = new float[9];

		getRotYX(angleX,angleY,c);

		return c;
	}
	//----------------------------------------------------------------------------
	public static float[] getRotZXY(float[] angle)
	{
		float[] c = new float[9];

		getRotZXY(angle,c);

		return c;
	}
	//----------------------------------------------------------------------------
	public static float[] getRotYXZ(float[] angle)
	{
		float[] c = new float[9];

		getRotYXZ(angle,c);

		return c;
	}
	//----------------------------------------------------------------------------
	public static float[] getRotAA(float angle, float[] v)
	{
		float[] c = new float[9];

		getRotAA(angle,v,c);

		return c;
	}

	//----------------------------------------------------------------------------
	/*
	private float boundedSum(float x,float y,float bound)
	{
		//if( x>bound ){ x = x - bound*(float)Math.floor(x/bound); }
		//else if( x<-bound ){ x = x + bound*(float)Math.floor(-x/bound); }

		float z = x + y;

		if( z>=bound ){ return bound; }
		else if( z<=-bound ){ return -bound; }
		else{ return z; }

		return z;
	}
	 */

	//----------------------------------------------------------------------------

}