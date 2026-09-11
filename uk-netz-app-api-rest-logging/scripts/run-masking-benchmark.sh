#!/usr/bin/env bash

set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
benchmark_cpus="${BENCHMARK_CPUS:-1}"
benchmark_memory="${BENCHMARK_MEMORY:-2g}"
benchmark_xms="${BENCHMARK_XMS:-512m}"
benchmark_xmx="${BENCHMARK_XMX:-1024m}"
benchmark_image="${BENCHMARK_IMAGE:-eclipse-temurin:21-jre}"
jmh_arguments=("$@")

if [[ "${1:-}" == "--help" ]]; then
    echo "Usage: scripts/run-masking-benchmark.sh [JMH arguments]"
    echo
    echo "Resource environment variables:"
    echo "  BENCHMARK_CPUS       Docker CPU quota (default: 1)"
    echo "  BENCHMARK_MEMORY     Docker memory and memory+swap limit (default: 2g)"
    echo "  BENCHMARK_XMS        JVM initial heap for every JMH fork (default: 512m)"
    echo "  BENCHMARK_XMX        JVM maximum heap for every JMH fork (default: 1024m)"
    echo "  BENCHMARK_IMAGE      Java 21 runtime image (default: eclipse-temurin:21-jre)"
    exit 0
fi

cd "$project_dir"
mvn -Pjmh -DskipTests clean package

if [[ ! -f "$project_dir/target/test-classes/META-INF/BenchmarkList" ]]; then
    echo "JMH benchmark metadata was not generated" >&2
    exit 1
fi
if [[ ! -d "$project_dir/target/jmh-libs" ]]; then
    echo "JMH dependency directory was not generated" >&2
    exit 1
fi

mkdir -p "$project_dir/target/jmh-results"

echo "Running with ${benchmark_cpus} CPU(s), ${benchmark_memory} memory, heap ${benchmark_xms}-${benchmark_xmx}"
docker run --rm \
    --cpus "$benchmark_cpus" \
    --memory "$benchmark_memory" \
    --memory-swap "$benchmark_memory" \
    --pids-limit 256 \
    --user "$(id -u):$(id -g)" \
    --read-only \
    --tmpfs /tmp:rw,nosuid,nodev,size=128m \
    --volume "$project_dir/target/classes:/opt/benchmark/classes:ro" \
    --volume "$project_dir/target/test-classes:/opt/benchmark/test-classes:ro" \
    --volume "$project_dir/target/jmh-libs:/opt/benchmark/jmh-libs:ro" \
    --volume "$project_dir/target/jmh-results:/opt/benchmark/results" \
    --workdir /opt/benchmark \
    "$benchmark_image" \
    java "-Xms${benchmark_xms}" "-Xmx${benchmark_xmx}" \
        -cp 'test-classes:classes:jmh-libs/*' \
        org.openjdk.jmh.Main \
        'uk.gov.netz.api.restlogging.MaskRewritePolicyBenchmark' \
        -prof gc \
        -foe true \
        -rf json \
        -rff results/masking-benchmark.json \
        "${jmh_arguments[@]}"

echo "JMH JSON result: target/jmh-results/masking-benchmark.json"
