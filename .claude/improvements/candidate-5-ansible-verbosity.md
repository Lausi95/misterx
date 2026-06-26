# Candidate 5 — Reduce Ansible verbosity in the deploy pipeline

**Strength:** Speculative  
**Effort:** Trivial — remove five characters from one file

## Problem

`.github/workflows/deploy.yml` runs Ansible with `-vvv`, which enables SSH
wire-level tracing. A typical deploy produces thousands of lines of protocol
noise. When a deploy fails, the actual error message is buried deep in the log
and requires manual scrolling to find.

```bash
# current (line 67 of deploy.yml)
ansible-playbook -vvv -e "ansible_user=tom" \
  --vault-password-file=vault_password.yml \
  --private-key /home/runner/.ssh/id_rsa \
  -i hosts.yml deploy-prod.yml
```

## Proposed solution

Drop `-vvv`. Default verbosity (no flag) shows task names and pass/fail status,
which is enough for normal operation. Re-add `-vvv` only when debugging a
specific failure by re-triggering the deploy workflow manually.

```bash
# after
ansible-playbook -e "ansible_user=tom" \
  --vault-password-file=vault_password.yml \
  --private-key /home/runner/.ssh/id_rsa \
  -i hosts.yml deploy-prod.yml
```

File: `.github/workflows/deploy.yml` — the `Run ansible script` step.

## Alternative: `-v` instead of no flag

`-v` (single) shows task output (stdout/stderr from shell commands) without SSH
traces — useful if you ever want to see what `docker compose up` prints without
the full wire protocol. A reasonable middle ground.

## How to explore in a future session

No grilling needed — just edit the file and commit. The only decision is
whether to go with no flag or `-v`.
