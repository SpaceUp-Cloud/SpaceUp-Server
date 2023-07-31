FROM ghcr.io/graalvm/graalvm-ce:22.3.3 AS graalvm
RUN gu install native-image

COPY . /home/app/SpaceUp
WORKDIR /home/app/SpaceUp

RUN native-image --no-server -cp build/libs/SpaceUp-*-all.jar

FROM frolvlad/alpine-glibc
RUN apk update && apk add libstdc++
EXPOSE 8080
COPY --from=graalvm /home/app/SpaceUp/SpaceUp /app/SpaceUp
ENTRYPOINT ["/app/SpaceUp"]
