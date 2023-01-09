load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_jvm_external",
    sha256 = "735602f50813eb2ea93ca3f5e43b1959bd80b213b836a07a62a29d757670b77b",
    strip_prefix = "rules_jvm_external-4.4.2",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/refs/tags/4.4.2.zip",
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

_JGRAPHT_VERSION = "1.5.1"

load("@rules_jvm_external//:defs.bzl", "maven_install")

# Defines the @unpinned_maven repo, which is the input to rules_jvm_external's dependency
# resolution. Whenever a dep in @unpinned_maven changes, run
# `REPIN=1 bazel run @unpinned_maven//:pin` to update maven_install.json.
maven_install(
    name = "maven",
    artifacts = [
        "com.beust:jcommander:1.72",
        "com.google.guava:guava:31.1-jre",
        "org.slf4j:slf4j-simple:2.0.6",
        "io.github.microutils:kotlin-logging-jvm:2.1.20",
        "org.jgrapht:jgrapht-core:" + _JGRAPHT_VERSION,
        "org.jgrapht:jgrapht-io:" + _JGRAPHT_VERSION,
    ],
    duplicate_version_warning = "error",
    fail_if_repin_required = True,
    fetch_sources = True,
    maven_install_json = "//:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    # don't allow deps into the interior of the maven graph.
    strict_visibility = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")

# Defines the @maven repo, which is the source of truth throughout the repo. It uses
# whatever is in maven_install.json.
pinned_maven_install()

http_archive(
    name = "rules_python",
    sha256 = "8c8fe44ef0a9afc256d1e75ad5f448bb59b81aba149b8958f02f7b3a98f5d9b\
4",
    strip_prefix = "rules_python-0.13.0",
    url = "https://github.com/bazelbuild/rules_python/archive/refs/tags/0.13.\
0.tar.gz",
)

load("@rules_python//python:repositories.bzl", "python_register_toolchains")

python_version = "3.10.6"

# Every python target in the repo uses this python interpreter.
python_register_toolchains(
    name = "python",
    # Available versions are listed in @rules_python//python:versions.bzl.
    python_version = python_version,
)

load("@python//:defs.bzl", "interpreter")
load("@rules_python//python:pip.bzl", "pip_parse")

# Install all python deps, using the python interpreter above.
# For each package foo listed in requirements.txt, this creates a @pip_foo repo
# with a top-level :pkg py_library target.
pip_parse(
    name = "pip",
    # Fail immediately if pip tries to compile any big numerical package from source.
    # By default, pip searches pypi for precompiled "wheels" (binary blobs) that match the
    # os and python version, falling back to a source install if no suitable wheel is found. While
    # this is appropriate for pure python packages, the numerical packages listed below are
    # difficult to compile reliably in a cross-platform way. Instead of hoping for the best, fail
    # fast if a wheel cannot be found.
    # This constraint means that the python_interpreter target above cannot be upgraded immediately
    # after a new python release; we must wait until the maintainers of these packages upload wheels
    # for the new version.
    extra_pip_args = [
        "--only-binary",
        "numpy,scikit-learn",
    ],
    python_interpreter_target = interpreter,
    quiet = False,
    requirements_lock = "//:requirements.txt",
)

load("@pip//:requirements.bzl", "install_deps")

install_deps()

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3",
    urls = [
        "https://github.com/bazelbuild/rules_kotlin/releases/download/v1.7.1/rules_kotlin_release.tgz",
    ],
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")

kotlin_repositories()

register_toolchains(":kotlin_toolchain")
