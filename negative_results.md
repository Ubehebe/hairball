This file discusses some techniques I have tried for breaking dependencies that did not work out.

# Minimum feedback arc set
The [minimum feedback arc set](https://en.wikipedia.org/wiki/Feedback_arc_set) (MFAS) is the
smallest set of edges that includes at least one edge from every cycle. Removing the MFAS removes
all dependency cycles in one fell swoop, so this is an attractive concept.

Unfortunately, MFAS is NP-complete. There are greedy heuristics for approximating it efficiently,
for example [this paper](https://github.com/zhenv5/breaking_cycles_in_noisy_hierarchies). I ported and integrated it into
hairball, but the computed arc sets were too big to be useful.

As a practical matter, we don't necessarily want to remove all cycles from the dependency graph at
once. What we really want is to identify a low-effort, high-impact change: a small set of edges
whose removal dramatically shrinks the size of the largest connected component. (In the best case,
removing a single edge could cut the size of the largest connected component in half.) The edges
identified by the greedy MFAS heuristic tended to shrink the largest connected component by only one
vertex at a time. Reorganizing a code base iteratively in this way would be a waste of time.

# Identifying the "best edge" to delete
With the above insight, I tried to identify the best single edge to remove. The heuristic I used was
to maximize the product of the sizes of the connected components created by removing the edge.

Unfortunately, the graphs I was working with had no such easy wins. In order to create more
connected components, it was necessary to remove more than one edge at a time. So I was forced to
consider pairs of edges, triples of edges, etc. Enumerating those and computing the connected
components induced by removing them quickly became intractable.
