
package com.example.wikiverse;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;


public class GLRenderer implements GLSurfaceView.Renderer
{


	private float[] cameraUp = new float[3];
	private float[] cameraFront = new float[3];
	private float[] cameraPos = new float[3];


	//----------------------------------------------------------------------------
	GLRenderer()
	{
		super();

		cameraUp[0]=cameraUp[2]=0.0f;
		cameraUp[1]=1.0f;

		cameraFront[0]=cameraFront[1]=0.0f;
		cameraFront[2]=-3.0f;

		cameraPos[0]=cameraPos[1]=0.0f;
		cameraPos[2]=0.0f;




	}
	//----------------------------------------------------------------------------
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);


		//gl.glShadeModel(gl.GL_SMOOTH);        // use smooth shading
		gl.glShadeModel(gl.GL_FLAT);
		gl.glEnable(gl.GL_DEPTH_TEST);        // hidden surface removal
		//gl.glEnable(gl.GL_BLEND);					// enable blending
		//gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);


		//gl.glHint(gl.GL_LINE_SMOOTH_HINT, gl.GL_FASTEST);
		//gl.glHint(gl.GL_POLYGON_SMOOTH_HINT, gl.GL_FASTEST);
		//gl.glHint(gl.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_FASTEST);

		gl.glEnable(gl.GL_LIGHTING);				// enable lighting
		float[] lightPosition = { 0.0f, 0.0f, 0.0f, 1.0f };
		float[] diffuseLight = { 0.9f, 0.9f, 0.9f, 1.0f };
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, diffuseLight, 0);
		//gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, diffuseLight, 0);
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, lightPosition, 0);
		gl.glEnable(gl.GL_LIGHT0);

		gl.glEnable(gl.GL_COLOR_MATERIAL);
		//float[] material = { 0.6f, 0.6f, 0.7f, 1.0f };
		//gl.glMaterialfv(gl.GL_FRONT, gl.GL_AMBIENT_AND_DIFFUSE, material, 0);


		float[] lightModel = { 0.6f, 0.6f, 0.7f, 1.0f };
		gl.glLightModelfv(gl.GL_LIGHT_MODEL_AMBIENT, lightModel, 0);


	}
	//----------------------------------------------------------------------------
	@Override
	public void onDrawFrame(GL10 gl)
	{

		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);


		gl.glLoadIdentity();

		GLU.gluLookAt(
				gl,
				cameraPos[0], cameraPos[1], cameraPos[2],
				cameraFront[0]+cameraPos[0], cameraFront[1]+cameraPos[1], cameraFront[2]+cameraPos[2],
				cameraUp[0], cameraUp[1], cameraUp[2]
				);


	}
	//----------------------------------------------------------------------------
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{

		gl.glViewport(0, 0, width, height);


		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		//gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
		GLU.gluPerspective(gl,54.0f, (float)width/(float)height, 0.01f, 10000.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}
	//----------------------------------------------------------------------------

	public void setCameraOrientation(float frontX, float frontY, float frontZ, float upX, float upY, float upZ)
	{
		cameraFront[0]=frontX;
		cameraFront[1]=frontY;
		cameraFront[2]=frontZ;

		cameraUp[0]=upX;
		cameraUp[1]=upY;
		cameraUp[2]=upZ;

	}
	//----------------------------------------------------------------------------
	public void setCameraPosition(float posX, float posY, float posZ)
	{
		cameraPos[0]=posX;
		cameraPos[1]=posY;
		cameraPos[2]=posZ;
	}
	//----------------------------------------------------------------------------

	public void setCamera(float posX, float posY, float posZ,
			float frontX, float frontY, float frontZ, float upX, float upY, float upZ)
	{


		cameraPos[0]=posX;
		cameraPos[1]=posY;
		cameraPos[2]=posZ;

		cameraFront[0]=frontX;
		cameraFront[1]=frontY;
		cameraFront[2]=frontZ;

		cameraUp[0]=upX;
		cameraUp[1]=upY;
		cameraUp[2]=upZ;


	}
	//----------------------------------------------------------------------------
}