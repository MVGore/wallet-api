#!/usr/bin/env python3
"""
Wallet API Concurrency Testing Script

Tests the API under high concurrency conditions (1000+ RPS).
Requires: requests library (pip install requests)
"""

import concurrent.futures
import time
import uuid
import statistics
import requests
import sys
from typing import List, Tuple, Optional

# Configuration
BASE_URL = "http://localhost:8080/api/v1"
WALLET_ID = str(uuid.uuid4())
NUM_THREADS = 100
REQUESTS_PER_THREAD = 10
TIMEOUT = 30

class TestResults:
    def __init__(self):
        self.success_count = 0
        self.failure_count = 0
        self.response_times: List[float] = []
        self.errors: List[str] = []

    def add_success(self, response_time: float):
        self.success_count += 1
        self.response_times.append(response_time)

    def add_failure(self, error: str):
        self.failure_count += 1
        self.errors.append(error)

    def print_summary(self):
        total = self.success_count + self.failure_count
        success_rate = (self.success_count / total * 100) if total > 0 else 0

        print("\n" + "="*60)
        print("TEST RESULTS SUMMARY")
        print("="*60)
        print(f"Total Requests: {total}")
        print(f"Successful: {self.success_count} ({success_rate:.2f}%)")
        print(f"Failed: {self.failure_count}")

        if self.response_times:
            print(f"\nResponse Time Statistics (ms):")
            print(f"  Min: {min(self.response_times):.2f}")
            print(f"  Max: {max(self.response_times):.2f}")
            print(f"  Avg: {statistics.mean(self.response_times):.2f}")
            print(f"  Median: {statistics.median(self.response_times):.2f}")
            if len(self.response_times) > 1:
                print(f"  Std Dev: {statistics.stdev(self.response_times):.2f}")

        if self.errors:
            print(f"\nTop Errors:")
            error_counts = {}
            for error in self.errors:
                error_counts[error] = error_counts.get(error, 0) + 1
            for error, count in sorted(error_counts.items(), key=lambda x: x[1], reverse=True)[:5]:
                print(f"  - {error}: {count}")

        print("="*60 + "\n")

def test_deposit(results: TestResults, operation_id: int) -> None:
    """Test deposit operation"""
    try:
        start_time = time.time()
        response = requests.post(
            f"{BASE_URL}/wallet",
            json={
                "walletId": WALLET_ID,
                "operationType": "DEPOSIT",
                "amount": 1.00
            },
            timeout=TIMEOUT
        )
        elapsed = (time.time() - start_time) * 1000  # Convert to ms

        if response.status_code == 200:
            results.add_success(elapsed)
        else:
            results.add_failure(f"HTTP {response.status_code}: {response.text}")
    except Exception as e:
        results.add_failure(f"Exception: {str(e)}")

def test_withdraw(results: TestResults, operation_id: int) -> None:
    """Test withdraw operation"""
    try:
        start_time = time.time()
        response = requests.post(
            f"{BASE_URL}/wallet",
            json={
                "walletId": WALLET_ID,
                "operationType": "WITHDRAW",
                "amount": 0.50
            },
            timeout=TIMEOUT
        )
        elapsed = (time.time() - start_time) * 1000  # Convert to ms

        if response.status_code == 200:
            results.add_success(elapsed)
        elif response.status_code == 400:
            # Insufficient funds is acceptable for this test
            results.add_success(elapsed)
        else:
            results.add_failure(f"HTTP {response.status_code}: {response.text}")
    except Exception as e:
        results.add_failure(f"Exception: {str(e)}")

def test_get_balance(results: TestResults, operation_id: int) -> None:
    """Test get balance operation"""
    try:
        start_time = time.time()
        response = requests.get(
            f"{BASE_URL}/wallets/{WALLET_ID}",
            timeout=TIMEOUT
        )
        elapsed = (time.time() - start_time) * 1000  # Convert to ms

        if response.status_code == 200:
            results.add_success(elapsed)
        else:
            results.add_failure(f"HTTP {response.status_code}: {response.text}")
    except Exception as e:
        results.add_failure(f"Exception: {str(e)}")

def run_test_suite() -> TestResults:
    """Run the complete test suite"""
    print("Starting Wallet API Concurrency Test")
    print(f"Target: {BASE_URL}")
    print(f"Wallet ID: {WALLET_ID}")
    print(f"Threads: {NUM_THREADS}")
    print(f"Requests per Thread: {REQUESTS_PER_THREAD}")
    print(f"Total Expected Requests: {NUM_THREADS * REQUESTS_PER_THREAD * 3}")
    print("-" * 60)

    results = TestResults()

    # Initialize wallet with deposit
    print("Initializing wallet with deposit...")
    try:
        response = requests.post(
            f"{BASE_URL}/wallet",
            json={
                "walletId": WALLET_ID,
                "operationType": "DEPOSIT",
                "amount": 10000.00
            },
            timeout=TIMEOUT
        )
        if response.status_code != 200:
            print(f"WARNING: Failed to initialize wallet: {response.status_code}")
    except Exception as e:
        print(f"WARNING: Exception initializing wallet: {e}")

    time.sleep(1)  # Wait for initialization

    # Run concurrent tests
    print("\nRunning concurrent tests...")
    start_time = time.time()

    with concurrent.futures.ThreadPoolExecutor(max_workers=NUM_THREADS) as executor:
        futures = []

        # Submit deposit operations
        for i in range(NUM_THREADS):
            for j in range(REQUESTS_PER_THREAD):
                futures.append(executor.submit(test_deposit, results, i * REQUESTS_PER_THREAD + j))

        # Submit withdraw operations
        for i in range(NUM_THREADS):
            for j in range(REQUESTS_PER_THREAD):
                futures.append(executor.submit(test_withdraw, results, i * REQUESTS_PER_THREAD + j))

        # Submit get balance operations
        for i in range(NUM_THREADS):
            for j in range(REQUESTS_PER_THREAD):
                futures.append(executor.submit(test_get_balance, results, i * REQUESTS_PER_THREAD + j))

        # Wait for all operations to complete
        concurrent.futures.wait(futures)

    total_time = time.time() - start_time
    rps = (results.success_count + results.failure_count) / total_time

    print(f"Tests completed in {total_time:.2f} seconds")
    print(f"Requests per second (RPS): {rps:.2f}")

    results.print_summary()

    return results

def run_stress_test(duration_seconds: int = 60) -> TestResults:
    """Run stress test for specified duration"""
    print(f"Starting Stress Test ({duration_seconds}s)")
    print(f"Target: {BASE_URL}")
    print(f"Wallet ID: {WALLET_ID}")
    print("-" * 60)

    results = TestResults()

    # Initialize wallet
    print("Initializing wallet...")
    try:
        requests.post(
            f"{BASE_URL}/wallet",
            json={
                "walletId": WALLET_ID,
                "operationType": "DEPOSIT",
                "amount": 100000.00
            },
            timeout=TIMEOUT
        )
    except Exception as e:
        print(f"WARNING: Exception initializing wallet: {e}")

    time.sleep(1)

    # Run stress test
    print(f"Running stress test for {duration_seconds} seconds...")
    start_time = time.time()
    end_time = start_time + duration_seconds

    with concurrent.futures.ThreadPoolExecutor(max_workers=200) as executor:
        futures = []
        operation_id = 0

        while time.time() < end_time:
            # Submit operations in batches
            for _ in range(50):
                futures.append(executor.submit(test_deposit, results, operation_id))
                operation_id += 1
                if time.time() >= end_time:
                    break

            time.sleep(0.1)  # Small delay between batches

        # Wait for remaining operations
        concurrent.futures.wait(futures)

    total_time = time.time() - start_time
    rps = (results.success_count + results.failure_count) / total_time

    print(f"Stress test completed in {total_time:.2f} seconds")
    print(f"Achieved RPS: {rps:.2f}")

    results.print_summary()

    return results

def main():
    """Main entry point"""
    if len(sys.argv) > 1:
        if sys.argv[1] == "stress":
            duration = int(sys.argv[2]) if len(sys.argv) > 2 else 60
            run_stress_test(duration)
        elif sys.argv[1] == "help":
            print("Usage: python3 concurrency_test.py [command]")
            print("\nCommands:")
            print("  (no args)  Run standard concurrency test")
            print("  stress N   Run stress test for N seconds (default: 60)")
            print("  help       Show this help message")
        else:
            print(f"Unknown command: {sys.argv[1]}")
            sys.exit(1)
    else:
        run_test_suite()

if __name__ == "__main__":
    main()
