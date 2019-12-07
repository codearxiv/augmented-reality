package com.example.wikiverse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;



public abstract class Shape extends BasicShape{


	protected FloatBuffer colorBuffer = null;
	protected FloatBuffer normalBuffer = null;

	//----------------------------------------------------------------------------
	protected Shape(){ }
	//----------------------------------------------------------------------------
	public Shape(int mode, int vertexCapacity)
	{
		resizeClientBuffers(vertexCapacity);
		this.mode = mode;
	}

	//----------------------------------------------------------------------------
	public Shape(GL11 gl, int mode, float[] vertices, float[] colors, float[] normals, Float scale)
	{

		super(gl,mode,vertices,scale);

		ByteBuffer bbColor = ByteBuffer.allocateDirect(colors.length<<2);
		bbColor.order(ByteOrder.nativeOrder());
		colorBuffer = bbColor.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);


		ByteBuffer bbNormal = ByteBuffer.allocateDirect(normals.length<<2);
		bbNormal.order(ByteOrder.nativeOrder());
		normalBuffer = bbNormal.asFloatBuffer();
		normalBuffer.put(normals);
		normalBuffer.position(0);



	}
	//----------------------------------------------------------------------------
	public void setColors(float r, float g, float b, float alpha)
	{

		for(int i=0, numColorCoords=4*numVertices; i<numColorCoords; i++){
			colorBuffer.put(i,r);
			colorBuffer.put(i+1,g);
			colorBuffer.put(i+2,b);
			colorBuffer.put(i+3,alpha);
		}

	}
	//----------------------------------------------------------------------------
	public void setColors(float[] colors, int offset)
	{
		int limit = Math.min(4*numVertices-offset, colors.length);

		colorBuffer.position(offset);
		colorBuffer.put(colors, 0, limit);
		colorBuffer.position(0);

	}

	//----------------------------------------------------------------------------
	public void setNormals(float[] normals, int offset)
	{
		int limit = Math.min(3*numVertices-offset, normals.length);

		normalBuffer.position(offset);
		normalBuffer.put(normals, 0, limit);
		normalBuffer.position(0);

	}
	//----------------------------------------------------------------------------
	@Override
	public void resizeClientBuffers(int vertexCapacity)
	{

		FloatBuffer oldVertexBuffer = vertexBuffer;
		FloatBuffer oldColorBuffer = colorBuffer;
		FloatBuffer oldNormalBuffer = normalBuffer;

		ByteBuffer bbVertex = ByteBuffer.allocateDirect(12*vertexCapacity);
		bbVertex.order(ByteOrder.nativeOrder());
		vertexBuffer = bbVertex.asFloatBuffer();
		vertexBuffer.limit(3*numVertices);

		ByteBuffer bbColor = ByteBuffer.allocateDirect(16*vertexCapacity);
		bbColor.order(ByteOrder.nativeOrder());
		colorBuffer = bbColor.asFloatBuffer();
		colorBuffer.limit(4*numVertices);

		ByteBuffer bbNormal = ByteBuffer.allocateDirect(12*vertexCapacity);
		bbNormal.order(ByteOrder.nativeOrder());
		normalBuffer = bbNormal.asFloatBuffer();
		normalBuffer.limit(3*numVertices);

		if(numVertices>0){

			if(oldVertexBuffer!=null){
				oldVertexBuffer.position(0);
				vertexBuffer.put(oldVertexBuffer);
				vertexBuffer.position(0);
			}

			if(oldColorBuffer!=null){
				oldColorBuffer.position(0);
				colorBuffer.put(oldColorBuffer);
				colorBuffer.position(0);
			}

			if(oldNormalBuffer!=null){
				oldNormalBuffer.position(0);
				normalBuffer.put(oldNormalBuffer);
				normalBuffer.position(0);
			}

		}

	}
	//----------------------------------------------------------------------------
}
