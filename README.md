# RenderingEngine

<a><img src="https://github.com/dmhacker/RenderingEngine/blob/master/renders/db9a349f-3e7f-4a18-b673-7d0bc3cf0f12.png" align="center"></a>

<sub><sup>The Stanford Dragon. It took the ray tracer approximately 2 minutes to render all 100,000 of its facets on my laptop with antialiasing (3x3 jittered supersampling) enabled. Without antialiasing, it would have taken 13 seconds. The pre-calculated kd-tree took approximately 500 milliseconds to generate.</sup></sub>

## Specifications

* Recursive ray tracer
* Multithreaded (default number of threads = system cores)
* Uses Phong shading
* Support for ray reflection
* Support for camera translations (zoom)
* Support for multiple light sources
* Support for spheres
* Support for triangular meshing
* Support for STL files
* Optimized using balanced kd-tree [O(n) generation, O(log n) traversal]
* Antialiasing (jittered antialiasing of variable sample size) for color smoothing [O(n^2) time]
* 's' key takes picture of current render 

## Future Improvements

* Use surface area heuristic (SAH) to produce a better kd-tree
* Vertex normal interpolation for mesh shading
* Camera rotation
* Support for refraction


