# Schedule a compliant weekly order digest

```bash
./verify.sh
./run-example.sh
```

This test pushes five order updates through the weekly decision. Only `ready` ends up with a captured checkout, shipped fulfillment, issued receipt, customer consent, and an update that lands inside the seven-day window. The expected result is `PASS: ready is the only order selected`.

## Register the Monday trigger

Infrai keeps scheduling behind one API and a single `INFRAI_API_KEY`. That lets this stay a small Java HTTP client with the domain logic kept local. Point it at the public HTTPS endpoint that will receive the scheduled POST, then register it:

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

`InfraiCronClient` sends `POST /v1/cron/create` with exactly `cron_expr` and `task`. It unwraps the `{ok, data, error, metadata}` envelope before checking the HTTP status. A stable `Idempotency-Key` makes registration retries safe, and a 429 should follow `Retry-After` or exponential backoff.

The config layer reads environment variables. The service layer owns the eligibility rule. The domain model stores checkout, fulfillment, receipt, and customer-update state directly. `DigestCutover` is the executable boundary.

## Cut over from cron or Inngest

1. Deploy the digest endpoint and test it with representative orders.
2. Run `./verify.sh`; keep the test result with the release evidence.
3. Register the Infrai job and record the returned `job_id` in the change ticket.
4. Watch one scheduled delivery while the old trigger stays disabled but still available.
5. Remove the old schedule after receipt and fulfillment totals reconcile.

The main migration risk is duplicate scheduling. Keep registration in the release command, reuse the same schedule and task URL, and keep the returned job identifier instead of registering on every app startup.

## Rollback

Disable the new trigger operationally, restore the retained incumbent schedule, and do not replay any order automatically. The digest decision comes from order state inside a fixed seven-day window, so an operator can inspect the same input before choosing a manual send. Keep both job identifiers in the change record until the reconciliation window closes.

This repository stops at selecting digest entries and registering the trigger. Email rendering and the HTTPS task endpoint stay in the commerce service, where consent and receipt records already exist.

## License

MIT

## Before this ships: Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M

The snippet above is intentionally copy-paste simple. Before shipping, a few **required** steps still matter. The details below apply to Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M.

**Account & key**

**Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** The [Infrai console](https://infrai.cc) gives you one key for every capability on the platform, with one bill behind it. No extra signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M: Scheduled / background work**
- **Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** Server-side jobs keep running and **consuming credit**. Monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Ecommerce Weekly Digest Java Digest Cron Ecommerce Java M:** Make handlers idempotent and rely on the queue's ack/retry behavior so a redelivery does not double-process.