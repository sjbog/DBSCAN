# DBSCAN
DBSCAN clustering optimized for multicore processing.

### Idea
If the distance of two points in any dimension is more than <i>eps</i>, than the total distance is more than <i>eps</i>

1. Sort by some dimension

2. Build a neighborhood map in parallel

  * Slide through sorted data, from lowest to highest. Sliding performed until neighbor_value <= curr_value + eps

  * Use array / indices to store neighbors lists; ConcurrentLinkedQueue holds density reachable points.

3. Use DFS (Depth First Search) to find clusters

#### Note: Uses parallel streams, thus requires Java 8