package com.bitwiseops.lwjglutils;

public interface PolygonBuilder {
	public void beginShape();

	public void endShape();

	public void addVertex(float x, float y, float z);

	public void setColor(float r, float g, float b);

	public void setTextureCoords(float s, float t);

	public void setNormal(float nx, float ny, float nz);
}
