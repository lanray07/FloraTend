param([switch]$BuildDeviceTests)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$names = @('FLORATEND_KEYSTORE','FLORATEND_STORE_PASSWORD','FLORATEND_KEY_ALIAS','FLORATEND_KEY_PASSWORD')
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
    foreach ($name in $names) { if (![Environment]::GetEnvironmentVariable($name,'Process')) { throw "Missing signing variable: $name" } }
    $gradleArgs = @(':app:assembleRelease','--console=plain')
    $buildLog = 'evidence/signed-release-build.log'
    if ($BuildDeviceTests) {
        $gradleArgs += @('-Pfloratend.testBuildType=release', ':app:assembleReleaseAndroidTest')
        $buildLog = 'evidence/direct-release/instrumentation-build.log'
        [System.IO.Directory]::CreateDirectory((Join-Path $projectRoot 'evidence/direct-release')) | Out-Null
    }
    & .\gradlew.bat @gradleArgs *> $buildLog
    if ($LASTEXITCODE -ne 0) { throw "Signed release build failed; see $buildLog" }
    Write-Output 'Signed release APK built. Signing secrets remain outside the repository in Windows-protected local configuration.'
} finally {
    foreach ($name in $names) { [Environment]::SetEnvironmentVariable($name,$previous[$name],'Process') }
}
