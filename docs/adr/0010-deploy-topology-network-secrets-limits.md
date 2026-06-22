# 10. Deploy hardening: private network, vault-templated secrets, resource limits

Date: 2026-06-22

## Status

Accepted

## Context

The Compose production stack ran on a **shared** private server (`lausi95.net`, ~8GB RAM, low
overall load) that also hosts Traefik and other apps on a shared external `traefik` network.
Three weaknesses needed fixing together because they describe the same deploy topology:

- **Network.** Both the API *and* Postgres joined the public-facing `traefik` network, and
  Postgres published host port `5432`. Anything else on the shared network — and anything that
  reached the host — could talk to the database.
- **Secrets.** `POSTGRES_PASSWORD` / `SPRING_DATASOURCE_PASSWORD` were the literal string
  `city-game`, committed to the repo — despite the deploy pipeline already carrying an
  `ANSIBLE_VAULT_PASSWORD`.
- **Resource footprint.** No container memory limits, so the JVM sized its heap off the *host's*
  total RAM and could starve neighbouring services on the shared box.

## Decision

**Network isolation.** Two networks: the external `traefik` network (API only, so the proxy can
route to it) and a private `backend` bridge network (API + Postgres). Postgres joins **only**
`backend` and publishes **no** host port. `backend` is left as a normal bridge (not
`internal: true`) to avoid a future foot-gun if a container there ever needs egress.

**Secrets via Ansible Vault.** `docker-compose.yml` becomes a Jinja template
(`docker-compose.yml.j2`) rendered on deploy from a vault-encrypted vars file. A single strong
random password feeds both the Postgres and datasource sides so they cannot drift. No credential
lives in git in plaintext.

**Resource limits (shared host).** API container `mem_limit: 1g` with `-XX:MaxRAMPercentage=75`
(~768MB heap, headroom for metaspace / threads / HikariCP). Postgres `mem_limit: 1g` with modest
`shared_buffers=256MB` / `effective_cache_size=512MB`. This leaves ~6GB for Traefik, the OS, and
other tenants of the box. Both services get `restart: unless-stopped`.

## Consequences

- Postgres is unreachable except by the API over the private network; the public surface is the
  API behind Traefik only.
- The DB password is rotated to a random value held only in the vault; rendering happens at
  deploy time, so the server's working compose file is the only place the plaintext exists.
- The JVM has a predictable footprint that cannot starve the shared host; `MaxRAMPercentage=75`
  (rather than the default container heuristic) reserves explicit non-heap headroom so a busy
  moment does not trip the container OOM-killer.
- The numbers are tuned for a *shared, low-load* host. A dedicated box would warrant
  2g/2g and a larger heap — revisit if the deployment model changes.
