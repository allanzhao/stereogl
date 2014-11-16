package com.bitwiseops.lwjglutils;

import org.lwjgl.opengl.ARBShaderObjects;

public final class GlUtils {
	private GlUtils() {
	}

	public static String getLogString(int id) {
		return ARBShaderObjects.glGetInfoLogARB(id, ARBShaderObjects
				.glGetObjectParameteriARB(id,
						ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}
}
