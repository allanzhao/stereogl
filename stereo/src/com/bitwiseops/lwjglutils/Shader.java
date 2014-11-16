package com.bitwiseops.lwjglutils;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
    private final Type type;
    private int id = 0;

    public Shader(Type type) {
        this.type = type;
    }

    public void create(String source) {
        try {
            id = ARBShaderObjects.glCreateShaderObjectARB(type.glShaderType);

            if(id == 0) {
                throw new RuntimeException(
                        "Created shader object has an id of 0");
            }

            ARBShaderObjects.glShaderSourceARB(id, source);
            ARBShaderObjects.glCompileShaderARB(id);

            if(ARBShaderObjects.glGetObjectParameteriARB(id,
                    ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                throw new RuntimeException("Error compiling shader: "
                        + GlUtils.getLogString(id));
            }
        } catch(Exception e) {
            destroy();
            throw e;
        }
    }

    public void destroy() {
        if(id != 0) {
            ARBShaderObjects.glDeleteObjectARB(id);
            id = 0;
        }
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public static enum Type {
        VERTEX(ARBVertexShader.GL_VERTEX_SHADER_ARB), FRAGMENT(
                ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

        public final int glShaderType;

        private Type(int glShaderType) {
            this.glShaderType = glShaderType;
        }
    }
}
