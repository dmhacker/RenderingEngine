# RenderingEngine

<a><img src="https://github.com/dmhacker/RenderingEngine/blob/master/renders/06a77d44-b66b-4933-a4ff-dcda2e8b0f6b.png" align="center"></a>

<sub><sup>The Stanford Dragon. It took the ray tracer approximately 150 seconds to render its 100,000 facets on my laptop with antialiasing and ray reflection enabled. Without antialiasing, it would have taken approximately 20 seconds.</sup></sub>

## Specifications

* Recursive ray tracer
* Multithreaded (default number of threads = system cores)
* Uses Phong shading
* Support for ambient, diffuse, and specular lighting.
* Support for ray reflection
* Support for camera translations (zoom)
* Support for multiple light sources
* Support for spheres
* Support for triangular meshing
* Support for STL files
* Support for vertex normal interpolation (uses barycentric coordinates)
* Optimized using balanced kd-tree [O(n) generation, O(log n) traversal]
* Antialiasing (jittered antialiasing on n x n subcells of pixel) for color smoothing [O(n^2) time]
* Support for both gaussian and box filters [no performance difference with either]
* 's' key takes picture of current render 

## Future Improvements

* Texture mapping
* Support for refraction
* Better camera rotation


