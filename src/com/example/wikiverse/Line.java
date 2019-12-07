package com.example.wikiverse;

import javax.microedition.khronos.opengles.GL11;



public class Line extends BasicShapeVertexOrder
{


	//----------------------------------------------------------------------------
	public Line(GL11 gl, float x1, float y1, float z1, float x2, float y2, float z2)
	{
		super(gl,GL11.GL_LINES,2);

		vertexBuffer.limit(6);
		vertexBuffer.put(x1);
		vertexBuffer.put(y1);
		vertexBuffer.put(z1);
		vertexBuffer.put(x2);
		vertexBuffer.put(y2);
		vertexBuffer.put(z2);
		vertexBuffer.position(0);

		numVertices = 2;
	}
	//----------------------------------------------------------------------------

}