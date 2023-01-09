load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")
load("@rules_python//python:defs.bzl", "py_binary")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "define_kt_toolchain")
load("@rules_python//python:pip.bzl", "compile_pip_requirements")

compile_pip_requirements(
    name = "requirements",
    timeout = "moderate",
    requirements_in = "requirements.in",
    requirements_txt = "requirements.txt",
)

define_kt_toolchain(
    name = "kotlin_toolchain",
    api_version = "1.7",
    experimental_report_unused_deps = "warn",
    experimental_strict_kotlin_deps = "warn",
    jvm_target = "17",
    language_version = "1.7",
)

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
