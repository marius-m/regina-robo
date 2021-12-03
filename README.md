# What is it?

Project leverages &rsquo;Liepa&rsquo; engine to synthesize text to speech (in lithuanian).

Running project starts a &rsquo;Spring&rsquo; service that listens to API calls to schedule translation mechanism through &rsquo;Docker&rsquo;. It creates endpoints for other mechanisms to hook into.

It uses &rsquo;Docker&rsquo; to launch linux environment to lauch the actual conversion mechanism.


# Resources

-   [Liepa synthesizer downloads](https://raštija.lt/liepa/paslaugos-vartotojams/sintezatorius-akliesiems/)


# How to run it?

To use the service you&rsquo;ll need multiple dependencies.

-   Clone project
-   Download / install Java11 ([Great open java builds](https://www.azul.com/downloads/?version=java-11-lts&os=macos&architecture=x86-64-bit&package=jdk))
-   Download / install Docker ([Docker on desktop](https://www.docker.com/products/docker-desktop))
-   Download / &rsquo;Liepa&rsquo; synthesizer ([Download link to Liepa](https://raštija.lt/liepa/paslaugos-vartotojams/sintezatorius-akliesiems/))
    -   You&rsquo;ll need to extract it yourself, as we need only executables
-   Extract &rsquo;Liepa&rsquo; to `{rootProject}/robo-docker-build/formatter`
    -   The `formatter` directory contains &rsquo;Liepa&rsquo; service. In other words files like `{rootProject}/robo-docker-build/formatter/Liepa.exe`; `{rootProject}/.../formatter/LithUSStest.exe` and so on
-   Build docker image
    -   To build docker image go to `{rootProject}/robo-docker-build` and run `build.sh`
    -   This will create and store an image
    -   If you&rsquo;ll run `docker images` you should see something similar
        
        ```
        ❯ docker images
        REPOSITORY        TAG       IMAGE ID       CREATED       SIZE
        markmerkk/wine1   latest    0fcfa2f211a3   5 weeks ago   1.02GB
        ```
-   Run service
    -   Build the service with `{rootProject}./gradlew build`
    -   Easiest way to run it, execute `{rootProject}/robo-run.sh`
    -   Open IDE, import the project, run with additional properties
        
        ```
        -Dspring.profiles.active=dev -Dserver.port=8082 -DLOG_PATH=./logs -DtoolPath=./robo-docker-run -DoutPath=./tts_output -DdockerHost=localhost -DdockerPort=8082
        ```
-   Use API to run translation
    -   When using CLI `curl` you could use
        
        ```
        curl -X POST http://localhost:8082/api/process-run --data '{"inputText": "Super", "extraEntityId": "id1", "extraTextId": "id2"}' -H "Content-Type: application/json"
        ```
    -   Or import postman collection from the project (&ldquo;{{HOST}}&rdquo; variable is most likely &rsquo;http://localhost:8082&rsquo;)
-   Translation results can be found in `{rootProject}/tts_output/{random-uuid}/record.wav`


# How does it work (a bit in depth)

-   Launched service &rsquo;listens&rsquo; for direct API endpoint or messaging service (RabbitMQ built in) message to handle TTS process
-   When message comes
    -   It prepares files for translation (copies files to their right locations)
    -   It launches &rsquo;Docker&rsquo; image to begin processing
    -   If the text is long enough, it breaks down and joins all audio files together for longer record
    -   Cleans-up
    -   Copies results to provided location
