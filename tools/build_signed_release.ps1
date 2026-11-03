param(
    [switch]$BuildDeviceTests,
    [string]$PlantIdentifierUrl="https://floratend-plant-identification.lanraybanks.workers.dev/identify",
    [string]$LogPath="build/signed-release-build.log"
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$requiredNames = @('FLORATEND_KEYSTORE','FLORATEND_STORE_PASSWORD','FLORATEND_KEY_ALIAS','FLORATEND_KEY_PASSWORD')
$names = $requiredNames + @('FLORATEND_RECEIPT_VERIFIER_URL')
$previous = @{}
foreach ($name in $names) { $previous[$name] = [Environment]::GetEnvironmentVariable($name,'Process') }
try {
    if (!$env:FLORATEND_KEYSTORE) {
        $signingDirectory = Join-Path $env:USERPROFILE '.android'
        [System.IO.Directory]::CreateDirectory($signingDirectory) | Out-Null
        $storePath = Join-Path $signingDirectory 'floratend-release.p12'
        $secretPath = Join-Path $signingDirectory 'floratend-signing.dpapi.json'
        if ((Test-Path -LiteralPath $storePath) -and !(Test-Path -LiteralPath $secretPath)) { throw 'Existing FloraTend key found without its encrypted local configuration; supply the signing environment variables.' }
        if (Test-Path -LiteralPath $secretPath) {
            $secret = Get-Content -LiteralPath $secretPath -Raw | ConvertFrom-Json
            $bytes = [System.Security.Cryptography.ProtectedData]::Unprotect([Convert]::FromBase64String($secret.passwordCiphertext),$null,[System.Security.Cryptography.DataProtectionScope]::CurrentUser)
            $password = [System.Text.Encoding]::UTF8.GetString($bytes)
        } else {
            $random = [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
            $password = [Convert]::ToBase64String($random)
            $protected = [System.Security.Cryptography.ProtectedData]::Protect([System.Text.Encoding]::UTF8.GetBytes($password),$null,[System.Security.Cryptography.DataProtectionScope]::CurrentUser)
            @{alias='floratend-upload';passwordCiphertext=[Convert]::ToBase64String($protected);storePath=$storePath;protection='Windows DPAPI CurrentUser'} | ConvertTo-Json | Set-Content -LiteralPath $secretPath -Encoding utf8
        }
        $env:FLORATEND_KEYSTORE = $storePath
        $env:FLORATEND_STORE_PASSWORD = $password
        $env:FLORATEND_KEY_ALIAS = 'floratend-upload'
        $env:FLORATEND_KEY_PASSWORD = $password
        if (!(Test-Path -LiteralPath $storePath)) {
            & keytool -genkeypair -keystore $storePath -storetype PKCS12 -alias $env:FLORATEND_KEY_ALIAS -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=FloraTend' '-storepass:env' FLORATEND_STORE_PASSWORD '-keypass:env' FLORATEND_KEY_PASSWORD -noprompt
            if ($LASTEXITCODE -ne 0) { throw 'Signing key creation failed' }
        }
    }
    foreach ($name in $requiredNames) { if (![Environment]::GetEnvironmentVariable($name,'Process')) { throw "Missing signing variable: $name" } }
    # A signed candidate must have a verifier even when PowerShell supplies an
    # existing empty environment variable. Explicit LAT overrides remain valid.
    if ([string]::IsNullOrWhiteSpace($env:FLORATEND_RECEIPT_VERIFIER_URL)) {
        $env:FLORATEND_RECEIPT_VERIFIER_URL = 'https://floratend-receipt-verifier-production.lanraybanks.workers.dev/verify'
    }
    $gradleArgs = @(':app:assembleRelease','--console=plain')
    if($PlantIdentifierUrl) {
        if($PlantIdentifierUrl -notmatch '^https://[a-zA-Z0-9.-]+/identify$'){throw 'Invalid identification HTTPS endpoint'}
        $gradleArgs += ('-Pfloratend.plantIdentifierUrl='+$PlantIdentifierUrl)
    }
    $buildLog = $LogPath
    if ($BuildDeviceTests) {
        $gradleArgs += @('-Pfloratend.testBuildType=release', ':app:assembleReleaseAndroidTest')
        if (!$PSBoundParameters.ContainsKey('LogPath')) { $buildLog = 'build/signed-release-device-build.log' }
    }
    [System.IO.Directory]::CreateDirectory((Split-Path -Parent (Join-Path $projectRoot $buildLog))) | Out-Null
    & .\gradlew.bat @gradleArgs *> $buildLog
    if ($LASTEXITCODE -ne 0) { throw "Signed release build failed; see $buildLog" }
    # Verify the output too: a Gradle property can take precedence over the env.
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $apk = [IO.Compression.ZipFile]::OpenRead((Join-Path $projectRoot 'app/build/outputs/apk/release/app-release.apk'))
    $expectedFound = $false
    $unexpectedFound = $false
    $identifierFound = !$PlantIdentifierUrl
    $knownVerifiers = @('https://floratend-receipt-verifier-production.lanraybanks.workers.dev/verify','https://floratend-receipt-verifier.lanraybanks.workers.dev/verify')
    try {
        foreach ($entry in $apk.Entries | Where-Object { $_.FullName -match '^classes\d*\.dex$' }) {
            $reader = [IO.StreamReader]::new($entry.Open(),[Text.Encoding]::UTF8)
            try {
                $dex = $reader.ReadToEnd()
                if($PlantIdentifierUrl -and $dex.Contains($PlantIdentifierUrl)) { $identifierFound = $true }
                if ($dex.Contains($env:FLORATEND_RECEIPT_VERIFIER_URL)) { $expectedFound = $true }
                foreach ($knownVerifier in $knownVerifiers) {
                    if ($knownVerifier -ne $env:FLORATEND_RECEIPT_VERIFIER_URL -and $dex.Contains($knownVerifier)) { $unexpectedFound = $true }
                }
            } finally { $reader.Dispose() }
        }
    } finally { $apk.Dispose() }
    if(!$identifierFound){throw 'Signed APK identification endpoint does not match requested configuration'}
    if (!$expectedFound -or $unexpectedFound) { throw 'Signed APK verifier does not match the requested environment; candidate rejected.' }
    Write-Output 'Signed release APK built. Signing secrets remain outside the repository in Windows-protected local configuration.'
} finally {
    foreach ($name in $names) {
        if ($null -eq $previous[$name]) {
            [Environment]::SetEnvironmentVariable($name,[NullString]::Value,'Process')
        } else {
            [Environment]::SetEnvironmentVariable($name,$previous[$name],'Process')
        }
    }
}
