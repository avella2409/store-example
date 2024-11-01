if [[ "$1" == "--skip-tests" ]]; then
    echo "Skipping unit and integration tests as per the argument."
else
    echo "Running unit test"
    ../gradlew test -p ..

    echo "Running integration test"
    ../gradlew integration -p ..
fi

echo "Build docker image"
../gradlew bootBuildImage -p ..