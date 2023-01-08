from numpy import loadtxt
from sklearn.cluster import spectral_clustering
from sys import stdin
from argparse import ArgumentParser

if __name__ == "__main__":
    parser = ArgumentParser()
    parser.add_argument("--n_clusters", required=True)
    args = parser.parse_args()

    adjacency_matrix = loadtxt(stdin, dtype=int, ndmin=2)
    labels = spectral_clustering(adjacency_matrix, n_clusters=int(args.n_clusters))
    for i in labels:
        print(i)
