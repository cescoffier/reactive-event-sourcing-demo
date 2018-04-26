# Data generator

_Role_: Retrieve changes from the database (using Kafka) and dispath the data to AMQP and another rKafka topic

## Build

```bash
mvn clean compile
docker build -t rxes/data-dispatcher .
```

