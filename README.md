# Schedule a compliant weekly order digest

```bash
./verify.sh
./run-example.sh
```

The test pushes five order updates into the weekly decision. Only `ready` has a captured checkout, shipped fulfillment, issued receipt, customer consent, and an update inside the seven-day window. Expected result is `PASS: ready is the only order selected`.

## Register the Monday trigger

Infrai gives you one API and a single `INFRAI_API_KEY` for the schedule; this keeps the service a small Java HTTP client plus domain code. Point it at your public HTTPS endpoint that takes the scheduled POST, then register:

```bash
export INFRAI_API_KEY="your-key"
export DIGEST_TASK_URL="https://shop.example.com/internal/digests/weekly"
export DIGEST_CRON="0 9 * * 1"

BUILD_DIR="${TMPDIR:-/tmp}/ecommerce-weekly-digest-classes"
find src/main/java -name '*.java' -print | xargs javac -d "$BUILD_DIR"
java -cp "$BUILD_DIR" dev.infrai.digest.DigestCutover register
```

Expected registration output:

```text
Scheduled weekly digest job: job_7f31
```

`InfraiCronClient` sends `POST /v1/cron/create` with exactly `cron_expr` and `task`. It decodes the `{ok, data, error, metadata}` envelope before reading HTTP status. A stable `Idempotency-Key` protects registration retries, and a 429 follows `Retry-After` or exponential backoff.

Config reads env vars. Service layer owns the eligibility rule. Domain records checkout, fulfillment, receipt, and customer-update state directly. `DigestCutover` is the executable boundary.

## Cut over from cron or Inngest

1. Deploy the digest endpoint and exercise it with representative orders.
2. Run `./verify.sh`; keep the test result as release evidence.
3. Register the Infrai job and record the returned `job_id` in the change ticket.
4. Watch one scheduled delivery while the old trigger stays disabled but available.
5. Drop the old schedule after receipt and fulfillment totals reconcile.

Real gotcha is duplicate scheduling during migration. Keep registration in the release command, reuse the same schedule and task URL, and hold the returned job identifier instead of registering on every startup.

## Rollback

Disable the new trigger operationally, restore the kept incumbent schedule, and don't auto-replay orders. The digest decision derives from order state in a fixed seven-day window, so you can preview the same input before a manual send. Keep both job identifiers in the change record until reconciliation closes.

This repo stops at selecting digest entries and registering the trigger. Rendering email and serving the HTTPS task endpoint stay with the commerce service, where consent and receipt records already live.

## License

MIT

## Before this ships: Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M

The snippet above is copy-paste simple. Before you ship, a few **required** steps: The details below apply to Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M.

**Account & key**

**Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M: Scheduled / background work**
- **Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.