package com.example.wikiverse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


public class ShapeIndexed extends Shape
{
	protected ShortBuffer indexBuffer = null;
	protected int numIndices = 0;

	//----------------------------------------------------------------------------
	protected ShapeIndexed(){}
	//----------------------------------------------------------------------------
	public ShapeIndexed(GL11 gl, int mode, int vertexCapacity, int indexCapacity)
	{

		resizeClientBuffers(vertexCapacity);
		resizeClientIndexBuffer(indexCapacity);

		this.mode = mode;

		handles = new int[4];
		gl.glGenBuffers(4,handles,0);
	}
	//----------------------------------------------------------------------------
	public ShapeIndexed(GL11 gl, int mode,float[] vertices, float[] colors, float[] normals, short[] indices, Float scale)
	{

		super(gl,mode,vertices,colors,normals,scale);

		ByteBuffer bbIndice = ByteBuffer.allocateDirect(2*indices.length);
		bbIndice.order(ByteOrder.nativeOrder());
		indexBuffer = bbIndice.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);

		numIndices = indices.length;

		handles = new int[4];
		gl.glGenBuffers(4,handles,0);


	}
	//----------------------------------------------------------------------------
	public void mapToBuffer(GL11 gl)
	{

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[0]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 12*numVertices,vertexBuffer, GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[1]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 16*numVertices,colorBuffer, GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[2]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 12*numVertices,normalBuffer, GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, handles[3]);
		gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, 2*numIndices, indexBuffer, GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	//----------------------------------------------------------------------------
	public void bindToBuffer(GL11 gl)
	{
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[0]);
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[1]);
		gl.glColorPointer( 4, GL10.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, handles[2]);
		gl.glNormalPointer( GL10.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, handles[3]);


	}
	//----------------------------------------------------------------------------
	public void deleteFromBuffer(GL11 gl)
	{
		gl.glDeleteBuffers(4,handles,0);

	}
	//----------------------------------------------------------------------------
	public final void draw(GL11 gl)
	{
		gl.glDrawElements(mode, numIndices, GL11.GL_UNSIGNED_SHORT, 0);
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


		gl.glDrawElements(mode, numIndices, GL11.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);


	}

	//----------------------------------------------------------------------------
	public void put(ShapeIndexed shape, float posX, float posY, float posZ)
	{

		int oldNumVertices = numVertices;
		numVertices += shape.numVertices;

		if( 3*numVertices > vertexBuffer.capacity() ){
			resizeClientBuffers(2*numVertices);
		}

		int oldNumIndices = numIndices;
		numIndices += shape.numIndices;

		if( numIndices > indexBuffer.capacity() ){
			resizeClientIndexBuffer(2*numIndices);
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

		indexBuffer.limit(numIndices);
		indexBuffer.position(oldNumIndices);
		shape.indexBuffer.position(0);
		for(int i=0; i<shape.numIndices; i++){
			indexBuffer.put( (short)(shape.indexBuffer.get(i) + oldNumVertices) );
		}
		indexBuffer.position(0);
		shape.indexBuffer.position(0);


	}

	//----------------------------------------------------------------------------
	public void resizeClientIndexBuffer(int indexCapacity)
	{


		ShortBuffer oldIndexBuffer = indexBuffer;

		ByteBuffer bbIndex = ByteBuffer.allocateDirect(2*indexCapacity);
		bbIndex.order(ByteOrder.nativeOrder());
		indexBuffer = bbIndex.asShortBuffer();
		indexBuffer.limit(numIndices);

		if(numIndices>0){

			if(oldIndexBuffer!=null){
				oldIndexBuffer.position(0);
				indexBuffer.put(oldIndexBuffer);
				indexBuffer.position(0);
			}


		}

	}
	//----------------------------------------------------------------------------

	public static ShapeIndexed asIndexed(GL11 gl, Shape shape)
	{

		ShapeIndexed shapeIndexed = new ShapeIndexed(gl,shape.mode,shape.numVertices,shape.numVertices);

		shape.vertexBuffer.position(0);
		shapeIndexed.vertexBuffer.put(shape.vertexBuffer);
		shape.vertexBuffer.position(0);
		shapeIndexed.vertexBuffer.position(0);


		shape.colorBuffer.position(0);
		shapeIndexed.colorBuffer.put(shape.colorBuffer);
		shape.colorBuffer.position(0);
		shapeIndexed.colorBuffer.position(0);

		shape.normalBuffer.position(0);
		shapeIndexed.normalBuffer.put(shape.normalBuffer);
		shape.normalBuffer.position(0);
		shapeIndexed.normalBuffer.position(0);


		for(int i=0; i<shape.numVertices; i++){
			shapeIndexed.indexBuffer.put((short)i);
		}
		shapeIndexed.indexBuffer.position(0);


		shapeIndexed.numVertices = shape.numVertices;
		shapeIndexed.numIndices = shape.numVertices;
		shapeIndexed.mode = shape.mode;

		return shapeIndexed;

	}
	//----------------------------------------------------------------------------

}
