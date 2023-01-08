load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")
load("@rules_python//python:defs.bzl", "py_binary")

py_binary(
    name = "spectral_clustering",
    srcs = ["spectral_clustering.py"],
    deps = [
        "@pip_numpy//:pkg",
        "@pip_scikit_learn//:pkg",
    ],
)

kt_jvm_library(
    name = "lib",
    srcs = [
        "ClusteringImpl.kt",
        "GraphUtils.kt",
        "JavaClass.kt",
        "JdepsParsing.kt",
        "SpectralClustering.kt",
    ],
    data = [
        ":spectral_clustering",
    ],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        "@maven//:org_jgrapht_jgrapht_core",
        "@maven//:org_jgrapht_jgrapht_io",
    ],
)

kt_jvm_binary(
    name = "ClassScc",
    srcs = [
        "ClassScc.kt",
    ],
    main_class = "jvmutil.deps.ClassSccKt",
    runtime_deps = [
        "//logutil",
    ],
    deps = [
        ":lib",
        "@maven//:com_beust_jcommander",
        "@maven//:org_jgrapht_jgrapht_core",
    ],
)
