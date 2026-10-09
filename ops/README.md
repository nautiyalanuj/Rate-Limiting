<!-- //Copyright ANuj Nautiyal -->
# Optional local monitoring and storage

The application still starts with an ephemeral H2 database and no monitoring endpoints
or telemetry exports. Each feature is independent and explicitly opted in.
Run commands from the repository root. This Compose setup uses **Linux host networking**
for monitoring so the application can stay bound to `127.0.0.1`.
Docker Desktop needs host networking enabled; native Linux Docker supports it directly.

| Feature | Application flag | Compose profile |
| --- | --- | --- |
| Metrics/dashboard | `METRICS_ENABLED=true` or `--app.metrics.enabled=true` | `metrics` |
| OpenTelemetry traces | `TRACING_ENABLED=true` or `--app.tracing.enabled=true` | `tracing` |
| Persistent users in PostgreSQL | `--spring.profiles.active=postgres` | `storage` |

## Metrics and traces

```bash
docker compose --profile metrics --profile tracing up -d
METRICS_ENABLED=true TRACING_ENABLED=true ./mvnw spring-boot:run
```

For metrics alone, select only `--profile metrics` and set only `METRICS_ENABLED=true`.
For tracing alone, select only `--profile tracing` and set only `TRACING_ENABLED=true`.
Selecting a Compose profile does not change the separately running application's flags.

- Grafana: <http://127.0.0.1:3000>, user `admin`, initial password `local-demo-only`.
  Set `GRAFANA_PASSWORD` before first startup to choose another password.
- Dashboard: **Rate limiting → Local rate limiting**. It covers HTTP throughput and p95
  latency, admitted/rejected REST and MCP decisions, circuit state, heap, CPU and DB pool.
- Prometheus: <http://127.0.0.1:9090>. Scrapes <http://127.0.0.1:8080/actuator/prometheus>.
  All other actuator endpoints stay unavailable. If the app port changes, edit `prometheus.yaml`.
- Traces: Grafana **Explore → Tempo**; search with `{ resource.service.name = "rate-limiting-demo" }`.
  Incoming HTTP and outgoing weather HTTP calls use Spring's observation instrumentation.
  This does not enable JDBC query spans or telemetry log export.

Generate activity with `curl http://127.0.0.1:8080/demo/fixed` and repeat it to trigger 429s.
Allow several scrape/export intervals for graphs and traces to appear.
Both data sources are provisioned; a disabled backend will have no data. The metrics
dashboard requires the metrics profile even if Grafana was started for tracing alone.

Tracing samples every request by default for local use. Override with
`TRACING_SAMPLE_PROBABILITY=0.1`. Use `OTLP_TRACES_ENDPOINT` to change the HTTP exporter
endpoint (default `http://127.0.0.1:4318/v1/traces`). Disabling `TRACING_ENABLED` disables
the SDK and exporter. OTLP metric push is disabled; Prometheus collects metrics.
The collector batches traces and forwards them to Tempo. The collector queue is in memory,
so pending traces can be lost if it stops before export. Stored Tempo traces persist.

## Persistent application storage

```bash
export DB_PASSWORD='choose-a-local-password'
docker compose --profile storage up -d --wait
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

To combine all three, start all three Compose profiles and run:

```bash
METRICS_ENABLED=true TRACING_ENABLED=true ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

PostgreSQL uses the existing application's default database/user `postgres` on port 5432.
The container creates the `users` table on **first startup with an empty volume**;
the application validates the schema and never drops persistent tables.
Existing PostgreSQL installations are still supported with `DB_URL`, `DB_USERNAME`,
and `DB_PASSWORD` without using Compose. The PostgreSQL profile disables simulated demo routes.
If port 5432 is busy, set `POSTGRES_PORT=15432` for Compose and
`DB_URL=jdbc:postgresql://localhost:15432/postgres` for the application.
Rate-limit quotas and circuit state remain in memory and reset on application restart.

## Retention and stopping

Named Docker volumes persist PostgreSQL users, Grafana settings, Prometheus time series
(7-day retention), and Tempo traces (24-hour retention) across container restarts.
`docker compose --profile metrics --profile tracing --profile storage down` stops the stack
and retains those volumes. Adding `--volumes` permanently removes its stored data.
All listeners are local. Shut down the backend only after disabling its application flag
to avoid expected exporter warnings or scrape failures.

Implementation follows [Spring Boot tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html),
[Tempo configuration](https://grafana.com/docs/tempo/latest/configuration/), and
[Compose profiles](https://docs.docker.com/compose/how-tos/profiles/).
