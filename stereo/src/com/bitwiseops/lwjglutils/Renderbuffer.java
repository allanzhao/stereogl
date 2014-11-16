package com.bitwiseops.lwjglutils;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GLContext;

public class Renderbuffer {
	private int id = 0;
	private int width, height;
	
	public Renderbuffer() {

	}

	public void create(int width, int height, int internalFormat) {
		try {
			if(!GLContext.getCapabilities().GL_ARB_framebuffer_object) {
				throw new RuntimeException("Renderbuffer objects are not supported.");
			}
			
			id = ARBFramebufferObject.glGenRenderbuffers();
			
			bind();
			ARBFramebufferObject.glRenderbufferStorage(ARBFramebufferObject.GL_RENDERBUFFER, internalFormat, width, height);
			this.width = width;
			this.height = height;
		} catch(Exception e) {
			destroy();
			throw e;
		}
	}
	
	public void destroy() {
		if(id != 0) {
			ARBFramebufferObject.glDeleteRenderbuffers(id);
			id = 0;
		}
	}

	public void bind() {
		ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, id);
	}
	
	public static void bindNone() {
		ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, 0);
	}
	
	public int getId() {
		return id;
	}
	
	public int getWidth() {
		if(id != 0) {
			return width;
		} else {
			throw new IllegalStateException("Renderbuffer does not exist.");
		}
	}
	
	public int getHeight() {
		if(id != 0) {
			return height;
		} else {
			throw new IllegalStateException("Renderbuffer does not exist.");
		}
	}
}
