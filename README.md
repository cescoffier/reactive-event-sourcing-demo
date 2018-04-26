# Reactive Event Sourcing Demo - RxEs (pronounced REX)

## Prerequisites

create the `alerting/sr/main/resource/secrets.json` file before doing anything. This file contains:

```json
{
  "account": "twilio account number",
  "token": "twillio token",
  "sender": "twilio phone number as +33757919342",
  "to": "receiver phone number"
}
```

This file is ignored on purpose to not shared the credential publicly.

## Build

```bash
./build-all.sh
```

## Run

```bash
cd compose
docker-compose up
```

When stabilised, in another terminal:

```bash
cd compose
./init.sh
```

**IMPORTANT**: you need `httpie`

## Cleanup

```bash
cd compose
docker-compose down
```

