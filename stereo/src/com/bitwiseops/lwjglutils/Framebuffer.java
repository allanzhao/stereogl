package com.bitwiseops.lwjglutils;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class Framebuffer {
    private int id = 0;

    public Framebuffer() {

    }

    public void create() {
        if(!GLContext.getCapabilities().GL_EXT_framebuffer_object) {
            throw new RuntimeException("Framebuffer objects are not supported.");
        }

        id = ARBFramebufferObject.glGenFramebuffers();
    }

    public void destroy() {
        if(id != 0) {
            ARBFramebufferObject.glDeleteFramebuffers(id);
            id = 0;
        }
    }

    public void bind() {
        ARBFramebufferObject.glBindFramebuffer(
                ARBFramebufferObject.GL_FRAMEBUFFER, id);
    }

    public static void bindNone() {
        ARBFramebufferObject.glBindFramebuffer(
                ARBFramebufferObject.GL_FRAMEBUFFER, 0);
    }

    public void bindColorTexture(Texture2D texture) {
        ARBFramebufferObject.glFramebufferTexture2D(
                ARBFramebufferObject.GL_FRAMEBUFFER,
                ARBFramebufferObject.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
                texture.getId(), 0);
    }

    public void bindDepthTexture(Texture2D texture) {
        ARBFramebufferObject.glFramebufferTexture2D(
                ARBFramebufferObject.GL_FRAMEBUFFER,
                ARBFramebufferObject.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                texture.getId(), 0);
    }

    public void bindDepthRenderbuffer(Renderbuffer renderbuffer) {
        ARBFramebufferObject.glFramebufferRenderbuffer(
                ARBFramebufferObject.GL_FRAMEBUFFER,
                ARBFramebufferObject.GL_DEPTH_ATTACHMENT,
                ARBFramebufferObject.GL_RENDERBUFFER, renderbuffer.getId());
    }

    public void bindStencilRenderbuffer(Renderbuffer renderbuffer) {
        ARBFramebufferObject.glFramebufferRenderbuffer(
                ARBFramebufferObject.GL_FRAMEBUFFER,
                ARBFramebufferObject.GL_STENCIL_ATTACHMENT,
                ARBFramebufferObject.GL_RENDERBUFFER, renderbuffer.getId());
    }

    public int getId() {
        return id;
    }

    public boolean isComplete() {
        bind();
        return ARBFramebufferObject
                .glCheckFramebufferStatus(ARBFramebufferObject.GL_FRAMEBUFFER) == ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE;
    }
}
