package com.bitwiseops.lwjglutils;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderProgram {
    private final List<Shader> shaders = new ArrayList<>();
    private int id = 0;

    public ShaderProgram() {

    }

    public void create() {
        id = ARBShaderObjects.glCreateProgramObjectARB();

        if(id == 0) {
            throw new RuntimeException("Created program object has an id of 0");
        }
    }

    public void destroy() {
        if(id != 0) {
            ARBShaderObjects.glDeleteObjectARB(id);
            id = 0;
        }
    }

    public void attachShader(Shader shader) {
        shaders.add(shader);
        ARBShaderObjects.glAttachObjectARB(id, shader.getId());
    }

    public void link() {
        ARBShaderObjects.glLinkProgramARB(id);

        if(ARBShaderObjects.glGetObjectParameteriARB(id,
                ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            throw new RuntimeException("Error linking program: "
                    + GlUtils.getLogString(id));
        }
    }

    public void validate() {
        ARBShaderObjects.glValidateProgramARB(id);

        if(ARBShaderObjects.glGetObjectParameteriARB(id,
                ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            throw new RuntimeException("Error validating program: "
                    + GlUtils.getLogString(id));
        }
    }

    public void use() {
        ARBShaderObjects.glUseProgramObjectARB(id);
    }

    public static void useNone() {
        ARBShaderObjects.glUseProgramObjectARB(0);
    }

    public void setUniform(String name, float v0) {
        GL20.glUniform1f(GL20.glGetUniformLocation(id, name), v0);
    }

    public void setUniform(String name, float v0, float v1) {
        GL20.glUniform2f(GL20.glGetUniformLocation(id, name), v0, v1);
    }

    public void setUniform(String name, float v0, float v1, float v2) {
        GL20.glUniform3f(GL20.glGetUniformLocation(id, name), v0, v1, v2);
    }

    public void setUniform(String name, float v0, float v1, float v2, float v3) {
        GL20.glUniform4f(GL20.glGetUniformLocation(id, name), v0, v1, v2, v3);
    }

    public void setUniform(String name, int v0) {
        GL20.glUniform1i(GL20.glGetUniformLocation(id, name), v0);
    }

    public void setUniform(String name, int v0, int v1) {
        GL20.glUniform2i(GL20.glGetUniformLocation(id, name), v0, v1);
    }

    public void setUniform(String name, int v0, int v1, int v2) {
        GL20.glUniform3i(GL20.glGetUniformLocation(id, name), v0, v1, v2);
    }

    public void setUniform(String name, int v0, int v1, int v2, int v3) {
        GL20.glUniform4i(GL20.glGetUniformLocation(id, name), v0, v1, v2, v3);
    }

    public int getId() {
        return id;
    }
}
