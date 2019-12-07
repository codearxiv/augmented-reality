package com.example.wikiverse;

import javax.microedition.khronos.opengles.GL11;



public class ShapeVertexOrder extends Shape
{


	//----------------------------------------------------------------------------
	protected ShapeVertexOrder(){}
	//----------------------------------------------------------------------------
	public ShapeVertexOrder(GL11 gl, int mode, int vertexCapacity)
	{
		super(mode,vertexCapacity);

		handles = new int[3];
		gl.glGenBuffers(3,handles,0);
	}
	//----------------------------------------------------------------------------
	public ShapeVertexOrder(GL11 gl, int mode, float[] vertices, float[] colors, float[] normals, Float scale)
	{
		super(gl,mode,vertices,colors,normals,scale);

		handles = new int[3];
		gl.glGenBuffers(3,handles,0);
	}
	//----------------------------------------------------------------------------
	public void mapToBuffer(GL11 gl)
	{

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[0]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 12*numVertices, vertexBuffer, GL11.GL_STATIC_DRAW);


		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[1]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 16*numVertices, colorBuffer, GL11.GL_STATIC_DRAW);


		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[2]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 12*numVertices, normalBuffer, GL11.GL_STATIC_DRAW);


		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

	}

	//----------------------------------------------------------------------------
	public void bindToBuffer(GL11 gl)
	{


		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[0]);
		gl.glVertexPointer( 3, GL11.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[1]);
		gl.glColorPointer( 4, GL11.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[2]);
		gl.glNormalPointer( GL11.GL_FLOAT, 0, 0);


	}
	//----------------------------------------------------------------------------
	public void deleteFromBuffer(GL11 gl)
	{
		gl.glDeleteBuffers(3,handles,0);

	}
	//----------------------------------------------------------------------------
	public final void draw(GL11 gl)
	{
		gl.glDrawArrays( mode, 0, numVertices);

	}
	//----------------------------------------------------------------------------
	public void drawClientSide(GL11 gl)
	{
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);

		gl.glVertexPointer( 3, GL11.GL_FLOAT, 0, vertexBuffer);
		gl.glColorPointer( 4, GL11.GL_FLOAT, 0, colorBuffer);
		gl.glNormalPointer( GL11.GL_FLOAT, 0, normalBuffer);


		gl.glDrawArrays( mode, 0, numVertices);

		gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}
	//----------------------------------------------------------------------------
	public void put(Shape shape, float posX, float posY, float posZ)
	{
		int oldNumVertices = numVertices;
		numVertices += shape.numVertices;

		if( 3*numVertices > vertexBuffer.capacity() ){
			resizeClientBuffers(2*numVertices);
		}


		vertexBuffer.limit(3*numVertices);
		vertexBuffer.position(3*oldNumVertices);
		shape.vertexBuffer.position(0);
		for(int i=0, limit=3*shape.numVertices; i<limit; i+=3){
			vertexBuffer.put( shape.vertexBuffer.get(i) + posX );
			vertexBuffer.put( shape.vertexBuffer.get(i+1) + posY );
			vertexBuffer.put( shape.vertexBuffer.get(i+2) + posZ );
		}
		vertexBuffer.position(0);
		shape.vertexBuffer.position(0);

		colorBuffer.limit(4*numVertices);
		colorBuffer.position(4*oldNumVertices);
		shape.colorBuffer.position(0);
		colorBuffer.put(shape.colorBuffer);
		colorBuffer.position(0);
		shape.colorBuffer.position(0);

		normalBuffer.limit(3*numVertices);
		normalBuffer.position(3*oldNumVertices);
		shape.normalBuffer.position(0);
		normalBuffer.put(shape.normalBuffer);
		normalBuffer.position(0);
		shape.normalBuffer.position(0);


	}

	//----------------------------------------------------------------------------
}
