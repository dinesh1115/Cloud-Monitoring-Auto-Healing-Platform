# Cloud Monitoring Platform - Manual Testing Script
# This script provides automated manual testing for all API endpoints
# Usage: ./run-manual-tests.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Delay = 500,
    [switch]$Verbose
)

# Color functions
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Section { Write-Host "`n=== $args ===" -ForegroundColor Yellow }

# Test counter
$totalTests = 0
$passedTests = 0
$failedTests = 0

# Main test function
function Test-Endpoint {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Endpoint,
        [object]$Body,
        [int]$ExpectedStatus = 200
    )
    
    $totalTests++
    $url = "$BaseUrl$Endpoint"
    
    Write-Info "Test $totalTests: $TestName"
    Write-Info "  URL: $Method $url"
    
    try {
        $params = @{
            Uri         = $url
            Method      = $Method
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params["Body"] = $Body | ConvertTo-Json
            Write-Info "  Body: $($Body | ConvertTo-Json)"
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Success "  ✓ PASSED - Status: $($response.StatusCode)"
            $passedTests++
            return $response
        } else {
            Write-Error "  ✗ FAILED - Expected $ExpectedStatus, got $($response.StatusCode)"
            $failedTests++
        }
    } catch {
        Write-Error "  ✗ FAILED - Error: $($_.Exception.Message)"
        $failedTests++
    }
    
    Start-Sleep -Milliseconds $Delay
}

# =====================================================================
# TESTING SEQUENCE
# =====================================================================

Write-Section "HEALTH & SYSTEM CHECKS"

Test-Endpoint "Health Endpoint" "GET" "/actuator/health" $null 200

Write-Section "METRICS MANAGEMENT"

# Normal metric submission
$normalMetric = @{
    cpuUsage      = 45.5
    temperature   = 70.0
    memoryUsage   = 60.2
    diskUsage     = 55.8
}
Test-Endpoint "Submit Normal Metric" "POST" "/api/metrics" $normalMetric 200

# High CPU metric (should trigger anomaly)
$highCpuMetric = @{
    cpuUsage      = 92.0
    temperature   = 82.5
    memoryUsage   = 88.3
    diskUsage     = 85.0
}
Test-Endpoint "Submit High CPU Metric (Triggers Anomaly)" "POST" "/api/metrics" $highCpuMetric 200

# Another normal metric
$normalMetric2 = @{
    cpuUsage      = 55.0
    temperature   = 75.0
    memoryUsage   = 65.0
    diskUsage     = 60.0
}
Test-Endpoint "Submit Another Metric" "POST" "/api/metrics" $normalMetric2 200

# Retrieve all metrics
Test-Endpoint "Get All Metrics" "GET" "/api/metrics" $null 200

# Get specific metric
Test-Endpoint "Get Metric by ID" "GET" "/api/metrics/1" $null 200

# Filter metrics by CPU
Test-Endpoint "Filter Metrics by CPU Range" "GET" "/api/metrics?cpuMin=50&cpuMax=90" $null 200

# Sort metrics
Test-Endpoint "Sort Metrics by Temperature" "GET" "/api/metrics?sort=temperature&order=asc" $null 200

# Pagination
Test-Endpoint "Get Paginated Metrics" "GET" "/api/metrics?limit=2&offset=0" $null 200

Write-Section "ANOMALY DETECTION"

# Get all anomalies
Test-Endpoint "Get All Anomalies" "GET" "/api/anomalies" $null 200

# Manual anomaly analysis
$analysisMetric = @{
    cpuUsage      = 88.0
    temperature   = 79.0
    memoryUsage   = 85.0
    diskUsage     = 80.0
}
Test-Endpoint "Manual Anomaly Analysis" "POST" "/api/anomalies/analyze" $analysisMetric 200

Write-Section "MACHINE LEARNING"

# ML anomaly detection - normal metrics
$mlNormalMetric = @{
    cpuUsage      = 40.0
    temperature   = 60.0
    memoryUsage   = 50.0
    diskUsage     = 45.0
}
Test-Endpoint "ML Detection - Normal Metrics" "POST" "/api/ml/detect-anomaly" $mlNormalMetric 200

# ML anomaly detection - anomalous metrics
$mlAnomalousMetric = @{
    cpuUsage      = 99.0
    temperature   = 95.0
    memoryUsage   = 98.0
    diskUsage     = 97.0
}
Test-Endpoint "ML Detection - Anomalous Metrics" "POST" "/api/ml/detect-anomaly" $mlAnomalousMetric 200

# Get ML model info
Test-Endpoint "Get ML Model Information" "GET" "/api/ml/model-info" $null 200

Write-Section "RECOVERY & HEALING"

# Get recovery actions
Test-Endpoint "Get Recovery Actions" "GET" "/api/recovery/actions" $null 200

# Get recovery history
Test-Endpoint "Get Recovery History" "GET" "/api/recovery/history" $null 200

Write-Section "LOAD TESTING"

# Simple load test (10 seconds, 5 RPS)
Test-Endpoint "Run Simple Load Test" "POST" "/api/load-test/simulate?duration=10&rps=5&cpuVariation=3" $null 200

# Get metrics summary
Test-Endpoint "Get Load Test Metrics Summary" "GET" "/api/load-test/metrics-summary" $null 200

Write-Section "CLOUDWATCH INTEGRATION (Optional)"

# This will only work if AWS is enabled
$cloudwatchMetric = @{
    metricName = "CPUUsage"
    value      = 75.5
    unit       = "Percent"
}

# Try CloudWatch endpoint - may fail if not enabled
try {
    Test-Endpoint "Publish to CloudWatch" "POST" "/api/cloudwatch/publish" $cloudwatchMetric 200
} catch {
    Write-Info "CloudWatch endpoint not available (expected if disabled)"
}

Write-Section "TEST SUMMARY"

Write-Info "Total Tests:  $totalTests"
Write-Success "Passed:      $passedTests"
Write-Error "Failed:      $failedTests"

$successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 }
Write-Info "Success Rate: $successRate%"

if ($failedTests -eq 0) {
    Write-Success "`n✓ ALL TESTS PASSED!"
    exit 0
} else {
    Write-Error "`n✗ SOME TESTS FAILED"
    exit 1
}
