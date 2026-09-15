#!/bin/bash
# ==============================================================================
# The Stampede Simulator - Flash Sale Concurrency Test
# ==============================================================================
# This script applies Zero-Based Thinking to load testing. We do not assume 
# the lock works; we attempt to break it with 100 simultaneous POST requests.

# 1. Define the target API endpoint
TARGET_URL="http://localhost:8080/api/orders"

# 2. Create the exact JSON payload the frontend would send
PAYLOAD_FILE="payload.json"
cat <<EOF > $PAYLOAD_FILE
{
  "userId": "user_stress_tester",
  "productId": "prod_golden_ticket",
  "quantity": 1,
  "price": 99.99
}
EOF

echo "[⚡] Commencing the Divine Stampede..."
echo "[⚙️] Firing 100 requests concurrently to $TARGET_URL"

# 3. Execute Apache Benchmark (ab)
# -n 100 : Total number of requests to perform
# -c 100 : Number of multiple requests to make at a time (Concurrency)
# -p     : File containing data to POST
# -T     : Content-type header to use
ab -n 100 -c 100 -p $PAYLOAD_FILE -T application/json $TARGET_URL

echo "[✅] Stampede complete. Check your database to ensure only ONE order was saved!"

# 4. Clean up temporary payload file
rm $PAYLOAD_FILE
