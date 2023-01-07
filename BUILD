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
    name = "RecommendJarPartition",
    srcs = [
        "ClusteredGraph.kt",
        "DotNode.kt",
        "JdepsParsing.kt",
        "Main.kt",
        "NodeWithIndex.kt",
        "Package.kt",
        "SklearnDriver.kt",
    ],
    data = [
        ":spectral_clustering",
    ],
    main_class = "jvmutil.deps.MainKt",
    deps = [
        "//logutil",
        "@maven//:com_beust_jcommander",
        "@maven//:com_google_guava_guava",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
    ],
)
