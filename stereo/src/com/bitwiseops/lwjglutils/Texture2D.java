package com.bitwiseops.lwjglutils;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

public class Texture2D {
    private int id = 0;
    private int width, height;

    public Texture2D() {

    }

    public void create(int width, int height, int internalFormat,
            int pixelFormat, int type, ByteBuffer data) {
        try {
            id = GL11.glGenTextures();

            if(id == 0) {
                throw new RuntimeException("Created texture has an id of 0");
            }

            bind();
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width,
                    height, 0, pixelFormat, type, data);
            this.width = width;
            this.height = height;
        } catch(Exception e) {
            destroy();
            throw e;
        }
    }

    public void create(int width, int height, int internalFormat,
            int pixelFormat, int type, byte[] data) {
        create(width, height, internalFormat, pixelFormat, type,
                directByteBufferFromBytes(data));
    }

    public void createEmpty(int width, int height, int internalFormat,
            int pixelFormat, int type) {
        create(width, height, internalFormat, pixelFormat, type,
                (ByteBuffer) null);
    }

    public void replacePixels(int x, int y, int width, int height,
            int pixelFormat, int type, ByteBuffer data) {
        bind();
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height,
                pixelFormat, type, data);
    }

    public void replacePixels(int x, int y, int width, int height,
            int pixelFormat, int type, byte[] data) {
        replacePixels(x, y, width, height, pixelFormat, type,
                directByteBufferFromBytes(data));
    }

    public void destroy() {
        if(id != 0) {
            GL11.glDeleteTextures(id);
            id = 0;
        }
    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public static void bindNone() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        if(id != 0) {
            return width;
        } else {
            throw new IllegalStateException("Texture does not exist.");
        }
    }

    public int getHeight() {
        if(id != 0) {
            return height;
        } else {
            throw new IllegalStateException("Texture does not exist.");
        }
    }

    private static ByteBuffer directByteBufferFromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
}
