load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")

kt_jvm_library(
    name = "lib",
    srcs = [
        "DotUtils.kt",
        "JavaClass.kt",
        "JdepsParsing.kt",
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
    deps = [
        ":lib",
        "@maven//:org_jgrapht_jgrapht_core",
    ],
)
