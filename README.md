# RenderingEngine

<a><img src="https://github.com/dmhacker/RenderingEngine/blob/master/renders/75d33ebd-fc3d-4c0b-8ed6-03f4fda55b4f.png" align="center"></a>

<sub><sup>From left to right: the Stanford Dragon, the Mandelbulb, and the Stanford Bunny. It took the ray tracer approximately 150 seconds (2.5 minutes) to render their combined 1,064,374 facets on my laptop with antialiasing (3x3 jittered supersampling) enabled. Without antialiasing, it would have taken approximately 20 seconds. The pre-calculated kd-tree took 7-8 seconds to generate.</sup></sub>

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
* Antialiasing (jittered antialiasing on n x n subcells of pixel) for color smoothing [O(n^2) time]
* 's' key takes picture of current render 

## Future Improvements

* Use surface area heuristic (SAH) to produce a better kd-tree
* Vertex normal interpolation for mesh shading
* Camera rotation
* Support for refraction


