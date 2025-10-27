FROM eclipse-temurin:21-jdk

WORKDIR /rangiffler
ENV TZ=Europe/Moscow
COPY ./gradle ./gradle
COPY ./rangiffler-e-2-e-tests ./rangiffler-e-2-e-tests
COPY ./rangiffler-grpc-common ./rangiffler-grpc-common
COPY ./rangiffler-media-common ./rangiffler-media-common
COPY ./gradlew ./
RUN sed -i -e 's/\r$//' gradlew && chmod +x gradlew
COPY ./build.gradle ./
COPY ./settings.gradle ./
COPY ./gradle.properties ./

CMD ./gradlew testDocker --no-daemon -Dtest.env=docker -Duser.timezone=Europe/Moscow
