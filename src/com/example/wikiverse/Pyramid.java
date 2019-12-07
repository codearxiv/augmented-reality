package com.example.wikiverse;

import javax.microedition.khronos.opengles.GL11;

public class Pyramid extends ShapeIndexed
{

	static float[] vertices = {
		0.0f,  0.5f, 0.0f,
		-0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, 0.5f,
		-0.5f, -0.5f, 0.5f,
	};

	static float[] colors = {
		0.63671875f, 0.76953125f, 0.22265625f, 0f,
		0.63671875f, 0.76953125f, 0.22265625f, 0f,
		0.63671875f, 0.76953125f, 0.22265625f, 0f,
		0.63671875f, 0.76953125f, 0.22265625f, 0f,
		0.63671875f, 0.76953125f, 0.22265625f, 0f
	};


	static float[] normals = {
		0.0f,  1.0f, 0.0f,
		-0.5773502f, -0.5773502f, -0.5773502f,
		0.5773502f, -0.5773502f, -0.5773502f,
		0.5773502f, -0.5773502f, 0.5773502f,
		-0.5773502f, -0.5773502f, 0.5773502f,
	};


	static short [] indices = {0,1,2, 0,2,3, 0,3,4, 0,4,1, 4,3,1, 3,2,1};



	//----------------------------------------------------------------------------
	public Pyramid(GL11 gl)
	{
		super(gl,GL11.GL_TRIANGLES,vertices,colors,normals,indices,null);
	}
	//----------------------------------------------------------------------------
	public Pyramid(GL11 gl,Float scale)
	{
		super(gl,GL11.GL_TRIANGLES,vertices,colors,normals,indices,scale);

	}

	//----------------------------------------------------------------------------

}
