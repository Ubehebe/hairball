This directory contains examples of hairball run on a few open-source jars:

## hibernate-core-6.1.6.Final.jar
```
$ jdeps -v --regex 'org\.hibernate.*' hibernate-core-6.1.6.Final.jar |
    bazel run hairball -- ProposeClusters --n-clusters 200 |
    dot -Tsvg -o examples/hibernate.svg /dev/stdin
```

## spring-core-6.0.3
```
$ jdeps -v --regex 'org\.springframework.*' spring-core-6.0.3.jar |
    bazel run hairball -- ProposeClusters --n-clusters 50 |
    dot -Tsvg -o examples/spring.svg /dev/stdin
 ```

## kotlin-stdlib-1.8.0.jar
```
jdeps -v --multi-release base --regex 'kotlin.*' kotlin-stdlib-1.8.0.jar |
    bazel run hairball -- ProposeClusters --n-clusters 100 |
    dot -Tsvg -o examples/kotlin.svg /dev/stdin
```