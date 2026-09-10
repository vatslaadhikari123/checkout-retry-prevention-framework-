# Checkout Job Retry Prevention & Idempotency Framework

In our checkout jobs, some visitor records would fail repeatedly due to bad data or external errors, which wasted processing power and risked looping forever. Instead of cluttering our checkout business logic with retry counters and if-else checks, I used Google Guice AOP.

I created two custom annotations: one that stops the database from even querying records that already exceeded retry limits, and another that wraps individual checkouts to track attempt counts and catch errors. I also wrote integration tests in JUnit using Guice to verify that these interceptors and retry counters behave properly.

---

## 🛠 Tech Stack
* **Language:** Java 11 / Java 8
* **Dependency Injection & AOP:** Google Guice 5.1.0
* **Testing:** JUnit, Mockito

---

## 🏗 How It Works

1. **Proactive Exclusion (`@FilterBusObjWithMaxAttempts`)**:
   * Intercepts the batch query before database execution.
   * Checks the retry tracking datastore for entity IDs that hit the retry threshold.
   * Dynamically injects a `NOT IN (exhausted_ids)` clause into `SearchCriteria` so failed records are never loaded into memory.

2. **Reactive Attempt Tracking (`@UpdateBusObjRetryAttempts`)**:
   * Implements `MethodInterceptor` around advice on individual checkout methods.
   * Runs the operation via `invocation.proceed()`.
   * Automatically records and increments attempt counts upon completion or runtime exceptions.

---

## 🧪 Testing
Run the integration test suite via Maven:

```bash
mvn clean test
