To update the website:
  - Checkout the `website-source` branch
  - Do updates and apply desired changes
  - Create the website static files by executing the below task:
    ```shell
    ./gradlew showcase:wasmJsBrowserDistribution
    ```
  - Copy the generated files from *showcase/build/dist/wasmJs/productionExecutable/*
  - Checkout this (`website`) branch
  - Rebase it onto `website-source` branch
  - Replace the old files in *docs/* directory with those copied files
  - Force push the changes to `website` branch
