"""Generate a mix of banking API page reads and transfers."""

import argparse
import concurrent.futures
import random
import time

import requests


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default="http://localhost:8080")
    parser.add_argument("--workers", type=int, default=4)
    parser.add_argument("--interval", type=float, default=3, help="seconds between account page reads per worker")
    parser.add_argument("--transfer-interval", type=float, default=10, help="seconds between transfer attempts")
    parser.add_argument("--duration", type=int, default=0, help="seconds to run (0 means indefinitely)")
    args = parser.parse_args()
    deadline = time.monotonic() + args.duration if args.duration else None

    def read_worker(index):
        session = requests.Session()
        while deadline is None or time.monotonic() < deadline:
            try:
                # Alternate between the account summary and account-specific activity.
                choice = random.random()
                path = f"/accounts/{random.choice([1001, 1002, 1003])}" if choice < 0.5 else "/"
                response = session.get(args.url + path, timeout=20)
                response.raise_for_status()
                backend = response.headers.get("X-DB-Backend", "unknown")
                print(f"worker={index} {response.status_code} {response.url} backend={backend}", flush=True)
            except requests.RequestException as exc:
                print(f"worker={index} error={exc}", flush=True)
            time.sleep(args.interval)

    def transfer_worker():
        session = requests.Session()
        next_transfer = time.monotonic() + args.transfer_interval
        while deadline is None or time.monotonic() < deadline:
            delay = max(0, next_transfer - time.monotonic())
            if deadline is not None:
                delay = min(delay, max(0, deadline - time.monotonic()))
            time.sleep(delay)
            if deadline is not None and time.monotonic() >= deadline:
                break
            source = random.choice([1001, 1002, 1003])
            destination = random.choice([account for account in [1001, 1002, 1003] if account != source])
            try:
                response = session.post(args.url + "/transfer", data={
                    "source": source,
                    "destination": destination,
                    "amount": random.choice([0.01, 0.05, 0.10]), "memo": "loadgen demo",
                }, timeout=60, allow_redirects=False)
                backend = response.headers.get("X-DB-Backend", "unknown")
                print(f"transfer status={response.status_code} backend={backend}", flush=True)
            except requests.RequestException as exc:
                print(f"transfer error={exc}", flush=True)
            next_transfer = max(next_transfer + args.transfer_interval, time.monotonic() + args.transfer_interval)

    with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers + 1) as pool:
        futures = [pool.submit(read_worker, i + 1) for i in range(args.workers)]
        futures.append(pool.submit(transfer_worker))
        for future in futures:
            future.result()


if __name__ == "__main__":
    main()
