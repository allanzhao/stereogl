package com.bitwiseops.stereo;

import java.io.IOException;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitwiseops.lwjglutils.Framebuffer;
import com.bitwiseops.lwjglutils.Renderbuffer;
import com.bitwiseops.lwjglutils.Shader;
import com.bitwiseops.lwjglutils.ShaderProgram;
import com.bitwiseops.lwjglutils.Texture2D;
import com.bitwiseops.utils.Resources;

public class Stereo {
	private static final String MAKE_LINK_VERTEX_SHADER_SOURCE_PATH = "shaders/make_link.vsh";
	private static final String MAKE_LINK_FRAGMENT_SHADER_SOURCE_PATH = "shaders/make_link.fsh";
	private static final String PROP_COLOR_VERTEX_SHADER_SOURCE_PATH = "shaders/prop_color.vsh";
	private static final String PROP_COLOR_FRAGMENT_SHADER_SOURCE_PATH = "shaders/prop_color.fsh";
	
	private final static Logger logger = LoggerFactory.getLogger("stereo");

	private int prevDisplayWidth = 0, prevDisplayHeight = 0;
	private byte[] backgroundTextureData;
    private Random random = new Random();
	private int screenTextureWidth, screenTextureHeight;
	private int backgroundTextureHeight = 512;
	private int screenDpi = 96;
	private float observerDistance = 12.0f;// in inches
	private float eyeSeparation = 2.5f;// in inches
	private float frustumNear = 1.0f;
	private float frustumFar = 5.0f;
	private int minPixelSep = 40;// inclusive
	private int maxPixelSep = 70;// inclusive
	
	private Shader makeLinkVertexShader;
	private Shader makeLinkFragmentShader;
	private ShaderProgram makeLinkShaderProgram;
	private Shader propColorVertexShader;
	private Shader propColorFragmentShader;
	private ShaderProgram propColorShaderProgram;
	private Texture2D backgroundTexture;
	private Framebuffer depthFramebuffer;
	private Texture2D depthTexture;
	private Framebuffer linkFramebuffer;
	private Texture2D linkDistanceTexture;
	private Renderbuffer linkDepthStencilBuffer;
	private Framebuffer propFramebuffer;
	private Texture2D propColorTexture0;
	private Texture2D propColorTexture1;
	
	private float time = 0f;
	
	public Stereo() {
		
	}

	public void init() {
		try{
            Display.setTitle("Stereo GL");
            Display.setDisplayMode(new DisplayMode(1024, 512));
            Display.setVSyncEnabled(true);
            Display.setResizable(true);
            PixelFormat pixelFormat = new PixelFormat().withDepthBits(8).withStencilBits(8);
            Display.create(pixelFormat);
        }catch(LWJGLException e){
        	logger.error("Could not initialize display.", e);
        	System.exit(0);
        }
        
		logger.info("Display initialized.");
		
		// Configure depth test
		GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        
        // Configure stencil test
        GL11.glStencilFunc(GL11.GL_EQUAL, 0, -1);// Pixels pass only if the stencil value is still zero
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);// If a pixel is drawn, make the stencil value nonzero
        
        // Configure back face culling
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CW);
        
        /*
         * Set up shaders
         */
        
        makeLinkVertexShader = new Shader(Shader.Type.VERTEX);
        makeLinkFragmentShader = new Shader(Shader.Type.FRAGMENT);
        makeLinkShaderProgram = new ShaderProgram();
        propColorVertexShader = new Shader(Shader.Type.VERTEX);
        propColorFragmentShader = new Shader(Shader.Type.FRAGMENT);
        propColorShaderProgram = new ShaderProgram();
        
        try {
        	makeLinkVertexShader.create(Resources.getResourceAsString(MAKE_LINK_VERTEX_SHADER_SOURCE_PATH));
        	makeLinkFragmentShader.create(Resources.getResourceAsString(MAKE_LINK_FRAGMENT_SHADER_SOURCE_PATH));
        	makeLinkShaderProgram.create();
        	makeLinkShaderProgram.attachShader(makeLinkVertexShader);
        	makeLinkShaderProgram.attachShader(makeLinkFragmentShader);
        	makeLinkShaderProgram.link();
        	makeLinkShaderProgram.validate();
        	
        	propColorVertexShader.create(Resources.getResourceAsString(PROP_COLOR_VERTEX_SHADER_SOURCE_PATH));
        	propColorFragmentShader.create(Resources.getResourceAsString(PROP_COLOR_FRAGMENT_SHADER_SOURCE_PATH));
        	propColorShaderProgram.create();
        	propColorShaderProgram.attachShader(propColorVertexShader);
        	propColorShaderProgram.attachShader(propColorFragmentShader);
        	propColorShaderProgram.link();
        	propColorShaderProgram.validate();
        } catch(IOException e) {
        	logger.error("Could not load shader source code.", e);
        	System.exit(0);
        } catch(RuntimeException e) {
        	logger.error("GLSL shaders may not be available.", e);
        	System.exit(0);
        }
        
        logger.info("Shaders compiled.");
        
        /*
         * Create background texture
         */
        
        backgroundTexture = new Texture2D();
        backgroundTexture.createEmpty(ceilPowerOfTwo(maxPixelSep), backgroundTextureHeight, GL11.GL_RGB, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE);
        backgroundTexture.bind();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		Texture2D.bindNone();
		backgroundTextureData = new byte[backgroundTexture.getWidth() * backgroundTexture.getHeight() * 3];
        
		/*
		 * Create depth framebuffer
		 */
		
		depthFramebuffer = new Framebuffer();
		depthFramebuffer.create();
		
		/*
		 * Create link framebuffer
		 */
		
		linkFramebuffer = new Framebuffer();
		linkFramebuffer.create();
		
		/*
		 * Create color propagation framebuffer
		 */
		
		propFramebuffer = new Framebuffer();
		propFramebuffer.create();
		
		logger.info("Initialization complete.");
	}

	public void render() {
		int displayWidth = Display.getWidth();
		int displayHeight = Display.getHeight();
		float aspectRatio = (float) displayWidth / (float) displayHeight;

		if(displayWidth != prevDisplayWidth || displayHeight != prevDisplayHeight) {
			// Resize viewport
			GL11.glViewport(0, 0, displayWidth, displayHeight);
			screenTextureWidth = displayWidth;
			screenTextureHeight = displayHeight;
			
			if(depthTexture != null) {
				depthTexture.destroy();
				depthTexture = null;
			}
			if(linkDistanceTexture != null) {
				linkDistanceTexture.destroy();
				linkDistanceTexture = null;
			}
			if(linkDepthStencilBuffer != null) {
				linkDepthStencilBuffer.destroy();
				linkDepthStencilBuffer = null;
			}
			if(propColorTexture0 != null) {
				propColorTexture0.destroy();
				propColorTexture0 = null;
			}
			if(propColorTexture1 != null) {
				propColorTexture1.destroy();
				propColorTexture1 = null;
			}
			
			prevDisplayWidth = displayWidth;
			prevDisplayHeight = displayHeight;
		}
		
		/*if(depthTexture == null) {
			depthTexture = new Texture2D();
			depthTexture.createEmpty(displayWidth, displayHeight, GL11.GL_, pixelFormat, type);
			
			
			
			
		}*/
		if(linkDistanceTexture == null) {
			linkDistanceTexture = new Texture2D();
			linkDistanceTexture.createEmpty(displayWidth, displayHeight, GL11.GL_RGB8, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE);
			linkDistanceTexture.bind();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
		if(linkDepthStencilBuffer == null) {
			linkDepthStencilBuffer = new Renderbuffer();
			linkDepthStencilBuffer.create(displayWidth, displayHeight, ARBFramebufferObject.GL_DEPTH24_STENCIL8);
		}
		if(propColorTexture0 == null) {
			propColorTexture0 = new Texture2D();
			propColorTexture0.createEmpty(displayWidth, displayHeight, GL11.GL_RGB8, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE);
			propColorTexture0.bind();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
		if(propColorTexture1 == null) {
			propColorTexture1 = new Texture2D();
			propColorTexture1.createEmpty(displayWidth, displayHeight, GL11.GL_RGB8, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE);
			propColorTexture1.bind();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
		
		/*
		 * Generate new random background texture
		 */
		random.nextBytes(backgroundTextureData);
		backgroundTexture.replacePixels(0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight(), GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, backgroundTextureData);
        
		/*
		 * Create links
		 */
		
		linkFramebuffer.bind();
		linkFramebuffer.bindColorTexture(linkDistanceTexture);
		linkFramebuffer.bindDepthRenderbuffer(linkDepthStencilBuffer);
		linkFramebuffer.bindStencilRenderbuffer(linkDepthStencilBuffer);
		if(!linkFramebuffer.isComplete()) {
			logger.error("Link framebuffer is not complete.");
		}
		
		// Depth pass
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		
		GL11.glColorMask(false, false, false, false);// Do not write colors
		GL11.glStencilMask(0);// Do not write stencil
		
		// FIXME: begin test code
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glFrustum(-aspectRatio, aspectRatio, -1f, 1f, frustumNear, frustumFar);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		/*GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, (float)Math.sin(time * 6.0f));
		GL11.glRotatef(time * 30.0f, 0, 0, 1);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		GL11.glVertex3f(-1, -1, -4);
		GL11.glVertex3f(-1, 1, -4);
		GL11.glVertex3f(1, -1, -4);
		GL11.glVertex3f(1, 1, -4);
		GL11.glEnd();
		GL11.glPopMatrix();*/
		
		// Draw a rippling rectangle
		GL11.glBegin(GL11.GL_QUADS);
		int divs = 20;
		float size = 6.0f;
		for(int y = 0; y < divs - 1; y++) {
			for(int x = 0; x < divs - 1; x++) {
				float x0 = -size * 0.5f + (float)x / divs * size;
				float y0 = -size * 0.5f + (float)y / divs * size;
				float x1 = -size * 0.5f + (float)(x + 1) / divs * size;
				float y1 = -size * 0.5f + (float)(y + 1) / divs * size;
				float z00 = 0.8f * (float)Math.sin(x0 + y0 + time * 4f) - 4;
				float z01 = 0.8f * (float)Math.sin(x0 + y1 + time * 4f) - 4;
				float z11 = 0.8f * (float)Math.sin(x1 + y1 + time * 4f) - 4;
				float z10 = 0.8f * (float)Math.sin(x1 + y0 + time * 4f) - 4;
				GL11.glVertex3f(x0, y0, z00);
				GL11.glVertex3f(x0, y1, z01);
				GL11.glVertex3f(x1, y1, z11);
				GL11.glVertex3f(x1, y0, z10);
			}
		}
		GL11.glEnd();
		
		time += 1f / 60f;
		// end test code
		
		// "Color" (distance) pass
		
		GL11.glColorMask(true, true, true, true);// Write colors
		GL11.glStencilMask(-1);// Write stencil
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0f, displayWidth, 0f, displayHeight, 0f, 1f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		makeLinkShaderProgram.use();
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		
		// TODO: fix left offset, implement overwriting of previous links
		// Draw depth slices
		for(int pixelSep = maxPixelSep; pixelSep >= minPixelSep; pixelSep--) {
			float separation = (float)pixelSep / screenDpi;
			float depth = (separation * observerDistance) / (eyeSeparation - separation);
			float z = -((frustumFar + frustumNear) * 0.5f / (frustumFar - frustumNear) + (-frustumFar * frustumNear) / ((frustumFar - frustumNear) * depth) + 0.5f);
			
			makeLinkShaderProgram.setUniform("pixelSep", (float)pixelSep);
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glVertex3f(maxPixelSep, 0.0f, z);
			GL11.glVertex3f(maxPixelSep, displayHeight, z);
			GL11.glVertex3f(displayWidth, 0.0f, z);
			GL11.glVertex3f(displayWidth, displayHeight, z);
			GL11.glEnd();
		}
		
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		Texture2D.bindNone();
		
		/*
		 * Color propagation
		 */
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		propFramebuffer.bind();
		propColorShaderProgram.use();
		propColorShaderProgram.setUniform("backgroundTexture", 0);
		propColorShaderProgram.setUniform("linkDistanceTexture", 2);
		propColorShaderProgram.setUniform("sourceTexture", 4);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		backgroundTexture.bind();
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		linkDistanceTexture.bind();
		propColorShaderProgram.setUniform("backgroundSize", (float)backgroundTexture.getWidth(), (float)backgroundTexture.getHeight());
		propColorShaderProgram.setUniform("screenTextureSize", (float)screenTextureWidth, (float)screenTextureHeight);
		
		int sourceTextureId = 0;
		int stripWidth = minPixelSep;
		for(int i = 0; i < (int)Math.ceil((float)displayWidth / stripWidth); i++) {
			Texture2D targetTexture = (sourceTextureId == 0) ? propColorTexture1 : propColorTexture0;
			Texture2D sourceTexture = (sourceTextureId == 0) ? propColorTexture0 : propColorTexture1;
			propFramebuffer.bindColorTexture(targetTexture);
			GL13.glActiveTexture(GL13.GL_TEXTURE4);
			sourceTexture.bind();
			propColorShaderProgram.setUniform("newStartX", (float)(i * stripWidth));
			
			if(i < 2) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			}
			
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0f, displayWidth, 0f, displayHeight, 0f, 1f);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			
			int minX = (i - 1) * stripWidth;
			int maxX = (i + 1) * stripWidth;// FIXME: clamp to bounds of screen
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(minX, 0.0f, 0.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(minX, displayHeight, 0.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(maxX, 0.0f, 0.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(maxX, displayHeight, 0.0f);
			GL11.glEnd();
			
			sourceTextureId = 1 - sourceTextureId;// ping-pong between textures
		}
		
		Texture2D.bindNone();
		ShaderProgram.useNone();
		
		// Need to display the texture that was rendered to last
		Texture2D finalTexture = (sourceTextureId == 0) ? propColorTexture0 : propColorTexture1;
		
		/*
		 * Display the final output
		 */
		
		Framebuffer.bindNone();
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0f, displayWidth, 0f, displayHeight, 0f, 1f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		finalTexture.bind();
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		GL11.glTexCoord2f(0.0f, 0.0f);
		GL11.glVertex3f(0.0f, 0.0f, 0.0f);
		GL11.glTexCoord2f(0.0f, 1.0f);
		GL11.glVertex3f(0.0f, displayHeight, 0.0f);
		GL11.glTexCoord2f(1.0f, 0.0f);
		GL11.glVertex3f(displayWidth, 0.0f, 0.0f);
		GL11.glTexCoord2f(1.0f, 1.0f);
		GL11.glVertex3f(displayWidth, displayHeight, 0.0f);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public void destroy() {
		makeLinkVertexShader.destroy();
		makeLinkFragmentShader.destroy();
		makeLinkShaderProgram.destroy();
		propColorVertexShader.destroy();
		propColorFragmentShader.destroy();
		propColorShaderProgram.destroy();
		backgroundTexture.destroy();
		linkFramebuffer.destroy();
		linkDistanceTexture.destroy();
		linkDepthStencilBuffer.destroy();
		propFramebuffer.destroy();
		propColorTexture0.destroy();
		propColorTexture1.destroy();
		Display.destroy();
	}

	public static void main(String[] args) {
		Stereo stereo = new Stereo();
		stereo.init();

		while(!Display.isCloseRequested()) {
			if(Display.isVisible()) {
				stereo.render();
				Display.update();
				Display.sync(60);
			}
		}

		stereo.destroy();
	}
	
	/**
	 * Returns the smallest power of two greater than or equal to <code>x</code>.
	 * @param x
	 * @return smallest power of two >= <code>x</code>
	 */
	private static int ceilPowerOfTwo(int x) {
		int po2 = 1;
		while(po2 < x) {
			po2 *= 2;
		}
		return po2;
	}
}
