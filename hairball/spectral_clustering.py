from numpy import loadtxt
from sklearn.cluster import spectral_clustering
from sys import stdin
from argparse import ArgumentParser

# Driver for scikit-learn's spectral_clustering method. Invoked from SklearnSpectralClustering.kt.
if __name__ == "__main__":
    parser = ArgumentParser()
    parser.add_argument("--n_clusters", required=True)
    parser.add_argument("--assign_labels", required=True)
    args = parser.parse_args()

    # SklearnSpectralClustering.kt writes the input adjacency matrix to this process' stdin,
    # one row per line. loadtxt can parse this.
    adjacency_matrix = loadtxt(stdin, dtype=int, ndmin=2)

    labels = spectral_clustering(
        adjacency_matrix,
        n_clusters=int(args.n_clusters),
        assign_labels=args.assign_labels,
    )

    # Print the cluster labels, one per line. The first line is the cluster label of vertex 0, etc.
    # Parsed by SklearnSpectralClustering.kt.
    for i in labels:
        print(i)
