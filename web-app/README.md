# Web App

_Role_: Receive data from AMQP and forward is to sockJS. The data is displayed in a web app.

## Build

```bash
mvn clean compile
docker build -t rxes/web-app .
```

