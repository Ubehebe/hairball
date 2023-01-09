load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")
load("@rules_python//python:defs.bzl", "py_binary")

py_binary(
    name = "spectral_clustering",
    srcs = ["spectral_clustering.py"],
    deps = [
        "@pip_numpy//:pkg",
        "@pip_scikit_learn//:pkg",
    ],
)

kt_jvm_binary(
    name = "hairball",
    srcs = [
        "AssignLabels.kt",
        "ClusteringImpl.kt",
        "GraphUtils.kt",
        "JavaClass.kt",
        "JdepsParsing.kt",
        "Main.kt",
        "MinimalClusters.kt",
        "ProposeClusters.kt",
        "SklearnSpectralClustering.kt",
    ],
    data = [
        ":spectral_clustering",
    ],
    main_class = "hairball.MainKt",
    runtime_deps = [
        "@maven//:org_slf4j_slf4j_simple",
    ],
    deps = [
        "@maven//:com_beust_jcommander",
        "@maven//:com_google_guava_guava",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        "@maven//:org_jgrapht_jgrapht_core",
        "@maven//:org_jgrapht_jgrapht_io",
    ],
)
