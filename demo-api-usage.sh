#!/bin/bash

# Example Usage Script for Reactive Email Service
# This script demonstrates how to test the email service using curl

BASE_URL="http://localhost:8080"

echo "==================================="
echo "Reactive Email Service Demo"
echo "==================================="
echo ""

# 1. Create a user (triggers welcome email)
echo "1. Creating a new user (triggers welcome email)..."
curl -X POST "$BASE_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }' | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# 2. Get all users
echo "2. Fetching all users..."
curl -X GET "$BASE_URL/api/users" | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# 3. Update a user (triggers update email)
echo "3. Updating user with ID 1 (triggers update notification)..."
curl -X PUT "$BASE_URL/api/users/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com"
  }' | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# 4. Get specific user
echo "4. Fetching user with ID 1..."
curl -X GET "$BASE_URL/api/users/1" | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# 5. Create multiple users (demonstrates batching)
echo "5. Creating multiple users (demonstrates email batching)..."

for i in {1..5}; do
  curl -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"User $i\",
      \"email\": \"user$i@example.com\"
    }" &
done

wait

echo ""
echo "All users created!"
echo ""
echo "-----------------------------------"
echo ""

# 6. Delete a user (triggers goodbye email)
echo "6. Deleting user with ID 1 (triggers goodbye email)..."
curl -X DELETE "$BASE_URL/api/users/1"

echo ""
echo "-----------------------------------"
echo ""

# 7. Verify deletion
echo "7. Verifying deletion - fetching all users..."
curl -X GET "$BASE_URL/api/users" | jq '.'

echo ""
echo "==================================="
echo "Demo completed!"
echo "==================================="
echo ""
echo "Check application logs to see:"
echo "  - Email sending confirmations"
echo "  - Batching behavior"
echo "  - Async processing details"
echo "  - Retry attempts (if any)"
