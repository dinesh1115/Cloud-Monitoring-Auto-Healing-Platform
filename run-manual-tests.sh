#!/bin/bash

# Cloud Monitoring Platform - Manual Testing Script
# This script provides automated manual testing for all API endpoints
# Usage: ./run-manual-tests.sh [BASE_URL] [DELAY_MS]

BASE_URL="${1:-http://localhost:8080}"
DELAY_MS="${2:-500}"

# Color functions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test counter
total_tests=0
passed_tests=0
failed_tests=0

# Test function
test_endpoint() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local body=$4
    local expected_status=${5:-200}
    
    ((total_tests++))
    local url="$BASE_URL$endpoint"
    
    echo -e "${CYAN}Test $total_tests: $test_name${NC}"
    echo -e "${CYAN}  URL: $method $url${NC}"
    
    local response
    local http_code
    
    if [ -z "$body" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json")
    else
        echo -e "${CYAN}  Body: $body${NC}"
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$body")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body_response=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}  ✓ PASSED - Status: $http_code${NC}"
        ((passed_tests++))
    else
        echo -e "${RED}  ✗ FAILED - Expected $expected_status, got $http_code${NC}"
        ((failed_tests++))
    fi
    
    sleep "$(echo "scale=3; $DELAY_MS / 1000" | bc)"
}

# =====================================================================
# TESTING SEQUENCE
# =====================================================================

echo -e "${YELLOW}\n=== HEALTH & SYSTEM CHECKS ===${NC}"
test_endpoint "Health Endpoint" "GET" "/actuator/health" "" 200

echo -e "${YELLOW}\n=== METRICS MANAGEMENT ===${NC}"

# Normal metric
test_endpoint "Submit Normal Metric" "POST" "/api/metrics" \
'{
  "cpuUsage": 45.5,
  "temperature": 70.0,
  "memoryUsage": 60.2,
  "diskUsage": 55.8
}' 200

# High CPU metric
test_endpoint "Submit High CPU Metric" "POST" "/api/metrics" \
'{
  "cpuUsage": 92.0,
  "temperature": 82.5,
  "memoryUsage": 88.3,
  "diskUsage": 85.0
}' 200

# Another metric
test_endpoint "Submit Another Metric" "POST" "/api/metrics" \
'{
  "cpuUsage": 55.0,
  "temperature": 75.0,
  "memoryUsage": 65.0,
  "diskUsage": 60.0
}' 200

# Get all metrics
test_endpoint "Get All Metrics" "GET" "/api/metrics" "" 200

# Get specific metric
test_endpoint "Get Metric by ID" "GET" "/api/metrics/1" "" 200

# Filter by CPU
test_endpoint "Filter Metrics by CPU" "GET" "/api/metrics?cpuMin=50&cpuMax=90" "" 200

# Sort metrics
test_endpoint "Sort Metrics" "GET" "/api/metrics?sort=temperature&order=asc" "" 200

# Pagination
test_endpoint "Get Paginated Metrics" "GET" "/api/metrics?limit=2&offset=0" "" 200

echo -e "${YELLOW}\n=== ANOMALY DETECTION ===${NC}"

# Get anomalies
test_endpoint "Get All Anomalies" "GET" "/api/anomalies" "" 200

# Manual analysis
test_endpoint "Manual Anomaly Analysis" "POST" "/api/anomalies/analyze" \
'{
  "cpuUsage": 88.0,
  "temperature": 79.0,
  "memoryUsage": 85.0,
  "diskUsage": 80.0
}' 200

echo -e "${YELLOW}\n=== MACHINE LEARNING ===${NC}"

# ML detection - normal
test_endpoint "ML Detection - Normal" "POST" "/api/ml/detect-anomaly" \
'{
  "cpuUsage": 40.0,
  "temperature": 60.0,
  "memoryUsage": 50.0,
  "diskUsage": 45.0
}' 200

# ML detection - anomalous
test_endpoint "ML Detection - Anomalous" "POST" "/api/ml/detect-anomaly" \
'{
  "cpuUsage": 99.0,
  "temperature": 95.0,
  "memoryUsage": 98.0,
  "diskUsage": 97.0
}' 200

# Model info
test_endpoint "Get ML Model Info" "GET" "/api/ml/model-info" "" 200

echo -e "${YELLOW}\n=== RECOVERY & HEALING ===${NC}"

# Recovery actions
test_endpoint "Get Recovery Actions" "GET" "/api/recovery/actions" "" 200

# Recovery history
test_endpoint "Get Recovery History" "GET" "/api/recovery/history" "" 200

echo -e "${YELLOW}\n=== LOAD TESTING ===${NC}"

# Load test
test_endpoint "Run Load Test" "POST" "/api/load-test/simulate?duration=10&rps=5" "" 200

# Metrics summary
test_endpoint "Get Load Test Summary" "GET" "/api/load-test/metrics-summary" "" 200

echo -e "${YELLOW}\n=== TEST SUMMARY ===${NC}"

echo -e "${CYAN}Total Tests:  $total_tests${NC}"
echo -e "${GREEN}Passed:      $passed_tests${NC}"
echo -e "${RED}Failed:      $failed_tests${NC}"

if [ $total_tests -gt 0 ]; then
    success_rate=$(echo "scale=2; ($passed_tests * 100) / $total_tests" | bc)
    echo -e "${CYAN}Success Rate: $success_rate%${NC}"
fi

if [ $failed_tests -eq 0 ]; then
    echo -e "${GREEN}\n✓ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}\n✗ SOME TESTS FAILED${NC}"
    exit 1
fi
