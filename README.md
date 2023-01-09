# hairball

```
Usage: < path/to/jdeps.txt hairball [options] [command] [command options]
  Options:
    -h, --help

      Default: false
  Commands:
    ProposeClusters      attempt to break input jar into n acyclic clusters.
            writes a .dot graph of the proposed clusters to stdout.
      Usage: ProposeClusters [options]
        Options:
          --assign-labels
            pass custom assign_labels arg to sklearn.spectral_clustering. See https://scikit-learn.org/stable/modules/generated/sklearn.cluster.spectral_clustering.html.
            Default: kmeans
            Possible Values: [kmeans, discretize, cluster_qr]
        * --n-clusters
            target number of clusters to create. the actual number of clusters
            may be smaller; see README.md for discussion.

    MinimalClusters      break input jar into minimal acyclic clusters
            (=strongly connected components). writes a .dot graph to stdout.
      Usage: MinimalClusters
```

## Overview

Large compilation units are a common headache in software maintenance. They cause many problems,
including long incremental builds, large artifact size, and difficulty sharing code.

The traditional way to shrink a large compilation unit is to manually move some of its sources into
other compilation units. This requires knowledge of the application's structure and good judgment.
In the case of monolithic compilation units that have grown over time, this manual process often
runs into circular dependencies: it can feel like every source file imports every other source file,
making it impossible to extract a small subset of sources.

hairball automates some of this process. When pointed at a jar file, it computes a way to cluster
the classes in the jar into `--n-clusters` clusters, such that there are no circular dependencies
between clusters. The output is a `.dot` file describing a graph structure. You can then use
[graphviz](https://graphviz.org) to visualize the graph (e.g. `dot -Tsvg -o app.svg app.dot`).

## Installation

hairball is built with [bazel](https://bazel.build). Once you have bazel installed, clone the repo
and build the hairball binary:

```
$ git clone https://github.com/Ubehebe/hairball
$ cd hairball
$ bazel build hairball
```

This will automatically download and configure all the toolchains and dependencies (java, kotlin,
python) needed to run hairball, without touching any local versions of these toolchains. Then you
can run the binary:

```
$ < path/to/jdeps.txt bazel run hairball -- ProposeClusters --n-clusters 100 > app.dot
```

## Implementation

hairball first condenses the source graph into [strongly connected components](https://en.wikipedia.org/wiki/Strongly_connected_component).
This is the most granular possible way to decompose a compilation unit: into sets of sources, each
member of which imports all the others.

An extremely fine-grained dependency graph is rarely desirable, because each compilation unit
requires some boilerplate to set up (`build.gradle.kts`, etc.); you don't want one of these files
for each and every source file. Usually, what you want is a "reasonable" number of compilation
units: maybe 20 or 50, depending on the size of the application. To achieve this, hairball uses
[spectral clustering](https://en.wikipedia.org/wiki/Graph_partition#Spectral_partitioning_and_spectral_bisection)
to partition the connected components into a user-specified number of clusters (`--n-clusters`).
Because the clustering may introduce new circular dependencies, hairball re-condenses the clusters
into strongly connected components before writing the final graph, to remove the circular
dependencies. This means that the number of clusters in the output may be less than `--n-clusters`.

## Limitations

hairball can't break circular dependencies. Large compilation units often contain a subset of
sources that all import each other. hairball will warn about them, like so:

```
[main] INFO hairball.Main - condensed 298 classes into 245 strongly connected components. the
largest component contains 48 classes that all depend on one another. you will have to break these
dependencies manually.
```

Circular dependencies are the fundamental reason why shrinking a compilation unit is hard. hairball
helps by extracting the dependencies that can be extracted, letting you focus on the manual work of
breaking circular dependencies.

hairball outputs a proposed dependency structure; it doesn't rearrange your source tree to implement
that structure. (hairball has no access to your source code.) For the time being, you have to
manually translate hairball's output into edits to your `build.gradle.kts` files or however you
structure your dependency graph.

hairball only works on jar files at the moment, though its underlying algorithms should work for
any kind of dependency structure. Feel free to send a pull request to teach hairball about other
languages or build systems.