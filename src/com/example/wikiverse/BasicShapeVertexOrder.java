package com.example.wikiverse;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class BasicShapeVertexOrder extends BasicShape{



	//----------------------------------------------------------------------------
	protected BasicShapeVertexOrder(){}
	//----------------------------------------------------------------------------
	public BasicShapeVertexOrder(GL11 gl, int mode, int vertexCapacity)
	{
		super(mode,vertexCapacity);

		handles = new int[1];
		gl.glGenBuffers(1,handles,0);
	}
	//----------------------------------------------------------------------------
	public BasicShapeVertexOrder(GL11 gl, int mode, float[] vertices, float[] colors, float[] normals, Float scale)
	{
		super(gl,mode,vertices,scale);

		handles = new int[1];
		gl.glGenBuffers(1,handles,0);
	}
	//----------------------------------------------------------------------------
	public void mapToBuffer(GL11 gl)
	{

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[0]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 12*numVertices, vertexBuffer, GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

	}

	//----------------------------------------------------------------------------
	public void bindToBuffer(GL11 gl)
	{

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[0]);
		gl.glVertexPointer( 3, GL11.GL_FLOAT, 0, 0);

	}
	//----------------------------------------------------------------------------
	public void deleteFromBuffer(GL11 gl)
	{
		gl.glDeleteBuffers(1,handles,0);
	}
	//----------------------------------------------------------------------------
	public final void draw(GL11 gl)
	{
		gl.glDrawArrays( mode, 0, numVertices);
	}
	//----------------------------------------------------------------------------
	public void DrawClientSide(GL11 gl)
	{
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);

		gl.glVertexPointer( 3, GL11.GL_FLOAT, 0, vertexBuffer);

		gl.glDrawArrays( mode, 0, numVertices);

		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);


	}
	//----------------------------------------------------------------------------
	public void put(BasicShape shape, float posX, float posY, float posZ)
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



	}

	//----------------------------------------------------------------------------


}
