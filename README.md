# mb-demo-applications-petstore-analyzer-consumer

This is a demo project to show a contract-first event consumer and different testing types.

It exposes a single endpoint, `POST /api/process/event`, which accepts an event carrying an
`eventType` and a `payload` and acknowledges it with `202`. Events missing either are rejected with
`400`.

## build
```
mvn clean install

mvn clean install -DskipTests=true
```

## run locally
```
mvn clean install spring-boot:run
```
