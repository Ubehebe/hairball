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
        "DotUtils.kt",
        "JavaPackage.kt",
        "JdepsParsing.kt",
        "NodeWithIndex.kt",
    ],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
    ],
)

kt_jvm_binary(
    name = "RecommendJarPartition",
    srcs = [
        "RecommendJarPartition.kt",
        "SklearnDriver.kt",
    ],
    data = [
        ":spectral_clustering",
    ],
    main_class = "jvmutil.deps.RecommendJarPartitionKt",
    deps = [
        ":lib",
        "//logutil",
        "@maven//:com_beust_jcommander",
    ],
)

kt_jvm_binary(
    name = "PackageScc",
    srcs = [
        "PackageScc.kt",
    ],
    main_class = "jvmutil.deps.PackageSccKt",
    deps = [
        ":lib",
        "@maven//:org_jgrapht_jgrapht_core",
        "@maven//:org_jgrapht_jgrapht_guava",
        "@maven//:org_jgrapht_jgrapht_io",
    ],
)
