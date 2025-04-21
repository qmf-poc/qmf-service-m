## QMF PoC Service

### Build

```shell
mvn package
```
### Run

```shell
java -jar target/service-0.x.y.jar --help
```

Have a list of agents available (active=false) initially
```shell
java -jar target/service-0.x.y.jar -a agent1, agent2
```

logging
```shell
java -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO|DEBUG|TRACE -jar target/agent-0.1.0-SNAPSHOT.jar ...
```