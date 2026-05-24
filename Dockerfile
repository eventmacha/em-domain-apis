# ==============================================================================
# Event Macha – Multi-stage Dockerfile
# Builds a Lambda-ready ZIP artifact and packages it as a Docker image
# for local development / integration testing.
# ==============================================================================

# ── Stage 1: Maven Build ───────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies before copying source
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Lambda Runtime Image ─────────────────────────────────────────────
# Uses the AWS Lambda provided Java 21 runtime base image.
FROM public.ecr.aws/lambda/java:21

# Copy the Quarkus-produced function ZIP contents into the Lambda task root
COPY --from=builder /build/target/function.zip /tmp/function.zip
RUN cd ${LAMBDA_TASK_ROOT} && jar -xf /tmp/function.zip && rm /tmp/function.zip

# Lambda handler – Quarkus stream handler bridges API Gateway to JAX-RS
CMD ["io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"]
