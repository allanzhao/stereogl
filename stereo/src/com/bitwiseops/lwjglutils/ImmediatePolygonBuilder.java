package com.bitwiseops.lwjglutils;

import org.lwjgl.opengl.GL11;

public class ImmediatePolygonBuilder implements PolygonBuilder {

	@Override
	public void beginShape() {
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
	}

	@Override
	public void endShape() {
		GL11.glEnd();
	}

	@Override
	public void addVertex(float x, float y, float z) {
		GL11.glVertex3f(x, y, z);
	}

	@Override
	public void setColor(float r, float g, float b) {
		GL11.glColor3f(r, g, b);
	}

	@Override
	public void setTextureCoords(float s, float t) {
		GL11.glTexCoord2f(s, t);
	}

	@Override
	public void setNormal(float nx, float ny, float nz) {
		GL11.glNormal3f(nx, ny, nz);
	}
}
