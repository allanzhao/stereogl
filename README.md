stereogl
========

Generates single image random dot stereograms (SIRDs) in real time using GLSL shaders and OpenGL on the GPU. Capable of rendering arbitrary geometry composed of triangles.

The result looks like TV static, but it can be viewed like a normal single image stereogram. If you are viewing it correctly, it should look like a waving flag. The demo runs at 60 FPS in fullscreen (1440x900) on my laptop with Intel integrated graphics, so the hardware requirements should be minimal.

Currently a work in progress, so the source code is a bit messy.

#### Helpful references
  - http://www.techmind.org/stereo/stech.html - CPU implementation, has some enhancements that I want to implement later
  - http://http.developer.nvidia.com/GPUGems/gpugems_ch41.html - Starting point for the GPU implementation

#### Libraries
  - [LWJGL](http://www.lwjgl.org/) - OpenGL bindings
  - [SLF4J](www.slf4j.org/) - Logging framework

### Algorithm
A detailed description of the algorithms used will come soon.
