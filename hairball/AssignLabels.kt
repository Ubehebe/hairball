package hairball

/**
 * Possible values of the assign_labels argument to scikit-learn's spectral_clustering method. See
 * https://scikit-learn.org/stable/modules/generated/sklearn.cluster.spectral_clustering.html.
 */
enum class AssignLabels {
  kmeans,
  discretize,
  cluster_qr
}
