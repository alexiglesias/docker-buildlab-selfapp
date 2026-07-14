# Docker-buildlab-selfapp

Self-contained app (Spring Boot + MySQL) for practicing Docker multi-stage builds and Compose orchestration, with no need to clone any external repository.

## What the app does

A simple web page that lists users stored in MySQL and lets you add new ones via a form. Enough to demonstrate a real 3-tier architecture (Nginx -> Spring Boot -> MySQL) without the complexity of VProfile.

## Structure

```
docker-buildlab-selfapp/
+-- app/
|   +-- Dockerfile          # multi-stage: Maven build -> JRE runtime
|   +-- pom.xml
|   \-- src/main/java/...   # real, working Java code
+-- web/
|   +-- Dockerfile
|   \-- selfapp.conf         # reverse proxy Nginx -> app
+-- docker-compose.yml
\-- .dockerignore
```

## Build and start

```bash
docker compose build
docker compose up -d
docker compose ps
```

Visit `http://localhost`.

## Check multi-stage optimization

```bash
docker images | grep selfapp-lite
```

The final image contains only a JRE + the compiled `.jar` -- no Maven, no source code.

## Publish to Docker Hub

```bash
docker login
export DOCKERHUB_USER=your_username
docker compose build
docker push $DOCKERHUB_USER/selfapp-lite:v1
```

## Login

The app now requires authentication (Spring Security, in-memory users -- for demo only):

- `admin_self` / `admin_self` (role ADMIN)
- `test_self` / `test_self` (role USER)

## Included but not yet used services

`selfcache` (Memcached) and `selfmq` (RabbitMQ) are defined in `docker-compose.yml` so you can keep practicing multi-service orchestration, but the app does not connect to them yet. Optional exercises:

- Add `spring-boot-starter-cache` + Memcached config to cache the user list.
- Publish a message to RabbitMQ each time a new user is added.

## Difference from the VProfile-based approach

This project is 100% self-contained: no need to clone any third-party repository or import an external SQL dump. Spring Boot creates the table automatically (`spring.jpa.hibernate.ddl-auto=update`) the first time it starts against an empty database.
