param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Name = "JWT Test User",
    [string]$Email = "jwt.test@example.com",
    [string]$Password = "Passw0rd!123"
)

$ErrorActionPreference = "Stop"

function Invoke-ApiRequest {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("GET", "POST", "PUT", "DELETE")]
        [string]$Method,

        [Parameter(Mandatory = $true)]
        [string]$Uri,

        [hashtable]$Headers,
        [object]$Body
    )

    $params = @{
        Method      = $Method
        Uri         = $Uri
        ErrorAction = "Stop"
    }

    if ($Headers) {
        $params.Headers = $Headers
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = $Body | ConvertTo-Json -Depth 10
    }

    try {
        $response = Invoke-WebRequest @params
        $parsedBody = $null
        if ($response.Content) {
            try {
                $parsedBody = $response.Content | ConvertFrom-Json
            } catch {
                $parsedBody = $response.Content
            }
        }

        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Body       = $parsedBody
        }
    } catch {
        if (-not $_.Exception.Response) {
            throw $_
        }

        $statusCode = [int]$_.Exception.Response.StatusCode
        $rawBody = $null

        if ($_.Exception.Response -and $_.Exception.Response.GetResponseStream()) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $rawBody = $reader.ReadToEnd()
            $reader.Close()
        }

        $parsedBody = $rawBody
        if ($rawBody) {
            try {
                $parsedBody = $rawBody | ConvertFrom-Json
            } catch {
                $parsedBody = $rawBody
            }
        }

        return [pscustomobject]@{
            StatusCode = $statusCode
            Body       = $parsedBody
        }
    }
}

$registerPayload = @{
    name     = $Name
    email    = $Email
    password = $Password
}
$loginPayload = @{
    email    = $Email
    password = $Password
}

$registerResponse = Invoke-ApiRequest -Method POST -Uri "$BaseUrl/auth/register" -Body $registerPayload
if ($registerResponse.StatusCode -notin @(201, 409)) {
    throw "Register failed with status $($registerResponse.StatusCode). Body: $($registerResponse.Body | ConvertTo-Json -Compress)"
}
Write-Host "POST /auth/register -> $($registerResponse.StatusCode)"

$loginResponse = Invoke-ApiRequest -Method POST -Uri "$BaseUrl/auth/login" -Body $loginPayload
if ($loginResponse.StatusCode -ne 200 -or -not $loginResponse.Body.token) {
    throw "Login failed with status $($loginResponse.StatusCode). Body: $($loginResponse.Body | ConvertTo-Json -Compress)"
}
$token = $loginResponse.Body.token
Write-Host "POST /auth/login -> $($loginResponse.StatusCode)"
Write-Host "Token length: $($token.Length)"

$withoutTokenResponse = Invoke-ApiRequest -Method GET -Uri "$BaseUrl/tasks"
Write-Host "GET /tasks without token -> $($withoutTokenResponse.StatusCode)"

$withTokenResponse = Invoke-ApiRequest -Method GET -Uri "$BaseUrl/tasks" -Headers @{ Authorization = "Bearer $token" }
Write-Host "GET /tasks with valid token -> $($withTokenResponse.StatusCode)"

$invalidTokenResponse = Invoke-ApiRequest -Method GET -Uri "$BaseUrl/tasks" -Headers @{ Authorization = "Bearer ${token}invalid" }
Write-Host "GET /tasks with invalid token -> $($invalidTokenResponse.StatusCode)"

$failures = @()
if ($withoutTokenResponse.StatusCode -ne 401) {
    $failures += "Expected 401 for missing token, got $($withoutTokenResponse.StatusCode)."
}
if ($withTokenResponse.StatusCode -ne 200) {
    $failures += "Expected 200 for valid token, got $($withTokenResponse.StatusCode)."
}
if ($invalidTokenResponse.StatusCode -ne 403) {
    $failures += "Expected 403 for invalid token, got $($invalidTokenResponse.StatusCode)."
}

if ($failures.Count -gt 0) {
    throw ($failures -join "`n")
}

Write-Host "JWT flow verification passed."
