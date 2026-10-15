FROM public.ecr.aws/docker/library/maven:3.9-amazoncorretto-21 AS build
WORKDIR /task
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src src/
RUN mvn -q clean package -DskipTests

FROM public.ecr.aws/docker/library/amazoncorretto:21
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:1.1.0 /lambda-adapter /opt/extensions/lambda-adapter
ENV AWS_LWA_READINESS_CHECK_PATH=/actuator/health/liveness
WORKDIR /opt
COPY --from=build /task/target/api-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]