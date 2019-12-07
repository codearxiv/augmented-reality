
package com.example.wikiverse;

import javax.microedition.khronos.opengles.GL11;



public class Triangle extends ShapeVertexOrder
{

	static float[] vertices = {
		// in counterclockwise order:
		0.0f,  0.622008459f, 0.0f,
		-0.5f, -0.311004243f, 0.0f,
		0.5f, -0.311004243f, 0.0f
	};

	static float[] colors = {
		0.63671875f, 0.76953125f, 0.22265625f, 0.5f,
		0.63671875f, 0.76953125f, 0.22265625f, 0.5f,
		0.63671875f, 0.76953125f, 0.22265625f, 0.5f,
	};

	static float[] normals = {
		0.0f,  1.0f, 0.0f,
		-0.5f, -0.311004243f, 0.0f,
		0.5f, -0.311004243f, 0.0f
	};

	//----------------------------------------------------------------------------
	public Triangle(GL11 gl)
	{
		super(gl,GL11.GL_TRIANGLES,vertices,colors,normals,null);
	}
	//----------------------------------------------------------------------------
	public Triangle(GL11 gl,Float scale)
	{
		super(gl,GL11.GL_TRIANGLES,vertices,colors,normals,scale);

	}

	//----------------------------------------------------------------------------

}
