# Karaf + Kafka + Redis demo

## Overview

- **Service A**: REST CRUD for `Person` (id, name, age). On every request (GET/CREATE/UPDATE/DELETE) it produces a Kafka event.
- **Service B**: Kafka consumer. Depending on operation it writes/deletes data in Redis.
- Both services run on **Apache Karaf** as OSGi bundles.

## Build & test

From repo root:

```powershell
mvn -q clean verify
```

Coverage gate is enforced via JaCoCo: **>= 80%** instructions.

## Run locally via Docker/K8s

See `k8s/` and `service-a/Dockerfile`, `service-b/Dockerfile`.

### Build Docker images

```powershell
docker build -f service-a/Dockerfile -t ms/service-a:dev .
docker build -f service-b/Dockerfile -t ms/service-b:dev .
```

### Deploy to Kubernetes

```powershell
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/service-a.yaml
kubectl apply -f k8s/service-b.yaml
```

Note: the manifests use `imagePullPolicy: IfNotPresent` so you can run with locally built images (e.g. Docker Desktop Kubernetes).

### Test Service A

(assuming a port-forward)

```powershell
kubectl port-forward svc/service-a 8080:8080
```

Create:

```powershell
curl -Method POST http://localhost:8080/persons -Body '{"id":"1","name":"Alice","age":30}' -ContentType 'application/json'
```

Get:

```powershell
curl http://localhost:8080/persons/1
```

Update:

```powershell
curl -Method PUT http://localhost:8080/persons/1 -Body '{"name":"Alice","age":31}' -ContentType 'application/json'
```

Delete:

```powershell
curl -Method DELETE http://localhost:8080/persons/1
```
