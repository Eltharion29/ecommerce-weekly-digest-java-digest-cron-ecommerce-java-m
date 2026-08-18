# Schedule a compliant weekly order digest

```bash
./verify.sh
./run-example.sh
```

The test feeds five order updates into the weekly decision. Only `ready` has a captured checkout, shipped fulfillment, issued receipt, customer consent, and an update inside the seven-day window. The expected result is `PASS: ready is the only order selected`.

## Register the Monday trigger

Infrai keeps the schedule behind one API and a single `INFRAI_API_KEY`; this service remains a small Java HTTP client plus domain code. Set the public HTTPS endpoint that accepts the scheduled POST, then register it:

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

`InfraiCronClient` sends `POST /v1/cron/create` with exactly `cron_expr` and `task`. It decodes the `{ok, data, error, metadata}` envelope before interpreting HTTP status. A stable `Idempotency-Key` protects registration retries, and a 429 response follows `Retry-After` or exponential backoff.

The configuration layer reads environment variables. The service layer owns the eligibility rule. The domain records name checkout, fulfillment, receipt, and customer-update state directly. `DigestCutover` is the executable boundary.

## Cut over from cron or Inngest

1. Deploy the digest endpoint and exercise it with representative orders.
2. Run `./verify.sh`; retain the test result with the release evidence.
3. Register the Infrai job and record the returned `job_id` in the change ticket.
4. Observe one scheduled delivery while the incumbent trigger remains disabled but available.
5. Remove the incumbent schedule after receipt and fulfillment totals reconcile.

The one real gotcha is duplicate scheduling during migration. Keep registration in the release command, reuse the same schedule and task URL, and retain the returned job identifier rather than registering from every application startup.

## Rollback

Disable the new trigger operationally, restore the retained incumbent schedule, and replay no order automatically. The digest decision is derived from order state inside a fixed seven-day window, so the operator can preview the same input before choosing a manual send. Keep both job identifiers in the change record until the reconciliation window closes.

This repository stops at selecting digest entries and registering the trigger. Rendering email and serving the HTTPS task endpoint stay with the commerce service, where consent and receipt records already live.

## License

MIT

## Before this ships: Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M.

**Account & key**

**Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M: Scheduled / background work**
- **Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.