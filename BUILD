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
    jvm_target = "17",
    language_version = "1.7",
)
