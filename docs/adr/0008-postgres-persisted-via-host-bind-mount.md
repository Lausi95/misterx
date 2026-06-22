# 8. Postgres persisted via host bind mount; deploy prune scoped to images

Date: 2026-06-22

## Status

Accepted

## Context

The production deployment runs Postgres as a Compose service on a single private server
(`lausi95.net`) alongside Traefik and the API. Two pre-existing facts combined into a
data-loss hazard:

- `ansible/files/docker-compose.yml` declared the Postgres service with **no `volumes:`**, so
  its data lived in an *anonymous* volume tied to the container's lifecycle.
- `ansible/deploy-prod.yml` ended every deploy with `docker system prune --all -f`.

On each deploy the API image is replaced and containers are recreated; `system prune --all`
then reclaims dangling images **and orphaned anonymous volumes**. The net effect is that a
routine deploy could destroy the production database. Even absent the prune, an anonymous
volume is easy to lose and impossible to back up deliberately.

Options considered:

- **Named Docker volume.** Survives `up`/`down` and is untouched by `image prune`. Durable, but
  opaque: backups mean `docker run --volumes-from` gymnastics, and the data lives in Docker's
  internal storage rather than an obvious path.
- **Host bind mount.** Data lives at a known host path (`/srv/...`); trivial to back up (tar /
  `pg_dump`), explicit about where state is, and impossible to remove via any `docker prune`.
- **Managed Postgres off-box.** Most robust, but disproportionate for a single hobby-scale
  private server.

## Decision

Persist Postgres to a **host bind mount** rather than an anonymous or named volume. Because the
deploy runs as an unprivileged user (no sudo), the mount lives under the deploy directory as a
relative path (`./pgdata` → `~/city-game-backend.lausi95.net/pgdata`), not under `/srv`. The
official Postgres image's entrypoint starts as root inside the container and `chown`s the data
dir, so the bind mount needs no special host ownership beyond the directory existing.

Scope the deploy cleanup to images only: replace `docker system prune --all -f` with
`docker image prune -f`. Volume reclamation is removed from the deploy path entirely — nothing
in a routine deploy may touch persistent state.

Postgres is pinned to a fixed major (`postgres:17-alpine`), never `latest`, because a
bind-mounted data directory cannot survive a surprise major-version jump.

## Consequences

- The database survives deploys, container recreation, and image churn. Data has a known,
  backup-friendly home on the host.
- The host path must exist and be owned such that the Postgres container's user can write it;
  this is provisioned as part of the deploy (Ansible `file:` task).
- `docker image prune -f` still reclaims old API images, so disk does not grow unbounded, but it
  can never reclaim a volume.
- Automated backups (`pg_dump`) are **not** wired in yet — durability rests on the bind mount.
  A scheduled dump is a noted follow-up, not part of this decision.
- Pinning the Postgres major means upgrades are a deliberate, planned migration, not an
  accident of pulling `latest`.
