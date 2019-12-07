package com.example.wikiverse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;



public abstract class BasicShape {


	protected FloatBuffer vertexBuffer = null;
	protected int numVertices = 0;

	protected int[] handles = null;

	protected int mode;

	//----------------------------------------------------------------------------
	protected BasicShape(){ }
	//----------------------------------------------------------------------------
	public BasicShape(int mode, int vertexCapacity)
	{
		resizeClientBuffers(vertexCapacity);
		this.mode = mode;
	}

	//----------------------------------------------------------------------------
	public BasicShape(GL11 gl, int mode, float[] vertices, Float scale)
	{

		float[] newVertices;

		if( scale==null ){ newVertices = vertices; }
		else{
			newVertices = new float[vertices.length];
			for(int i=0; i<vertices.length; i++){ newVertices[i] = scale*vertices[i]; }
		}


		ByteBuffer bbVertex = ByteBuffer.allocateDirect(newVertices.length<<2);
		bbVertex.order(ByteOrder.nativeOrder());
		vertexBuffer = bbVertex.asFloatBuffer();
		vertexBuffer.put(newVertices);
		vertexBuffer.position(0);


		numVertices = newVertices.length/3;

		this.mode = mode;

	}

	//----------------------------------------------------------------------------
	public void setVertices(float[] vertices, int offset)
	{
		int limit = Math.min(3*numVertices-offset, vertices.length);

		vertexBuffer.position(offset);
		vertexBuffer.put(vertices, 0, limit);
		vertexBuffer.position(0);

	}

	//----------------------------------------------------------------------------
	public void resizeClientBuffers(int vertexCapacity)
	{

		FloatBuffer oldVertexBuffer = vertexBuffer;

		ByteBuffer bbVertex = ByteBuffer.allocateDirect(12*vertexCapacity);
		bbVertex.order(ByteOrder.nativeOrder());
		vertexBuffer = bbVertex.asFloatBuffer();
		vertexBuffer.limit(3*numVertices);

		if(numVertices>0){

			if(oldVertexBuffer!=null){
				oldVertexBuffer.position(0);
				vertexBuffer.put(oldVertexBuffer);
				vertexBuffer.position(0);
			}

		}

	}
	//----------------------------------------------------------------------------
}