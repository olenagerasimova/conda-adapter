# Conda adapter benchmarks

To run benchmarks:
 1. Install snapshot locally of `conda-adapter`: `mvn install`
 2. Build `conda-bench` project: `mvn package -f ./benchmarks`
 4. Create directory for tests and copy test resources to this directory
 5. Run benchmarks with `env BENCH_DIR=${test-dir} java -cp "benchmarks/target/benchmarks.jar" org.openjdk.jmh.Main ${bench-name}`, where `${test-dir}` is a directory with test data, and `${bench-name}` is a benchbmark name.

## Benchmarks
