FROM openjdk:17
COPY /target/BankTask-3.4.5.jar backend.jar
ENTRYPOINT ["java", "-jar", "backend.jar"]