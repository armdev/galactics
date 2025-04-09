#!/usr/bin/env bash

set -e

# Set the number of iterations
iterations=5
count=0

# Run 1000 times
while [ $count -lt $iterations ]; do
  # Run fortune command
  fortune

  cmatrix

  # Run sl command
  sl

  # Increment the counter
  count=$((count + 1))

  # Wait for 5 minutes (300 seconds) if you want to re-enable the sleep
  # sleep 300
done
