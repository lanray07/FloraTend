$ErrorActionPreference='Stop'
$privatePath=Join-Path $env:USERPROFILE '.android/floratend-deepl-secrets.dpapi'
if(!(Test-Path -LiteralPath $privatePath)){throw 'Private DeepL API Free configuration is missing; use configure_deepl_key.ps1 locally. Do not paste a key into chat.'}
$bytes=[Security.Cryptography.ProtectedData]::Unprotect([IO.File]::ReadAllBytes($privatePath),$null,[Security.Cryptography.DataProtectionScope]::CurrentUser)
$oldKey=$env:DEEPL_AUTH_KEY
$oldEndpoint=$env:DEEPL_ENDPOINT
try {
    $configuration=[Text.Encoding]::UTF8.GetString($bytes)|ConvertFrom-Json
    if($configuration.provider -ne 'deepl' -or $configuration.plan -ne 'free' -or $configuration.endpoint -ne 'https://api-free.deepl.com' -or [string]::IsNullOrWhiteSpace($configuration.apiKey)){throw 'Expected private API Free configuration'}
    $env:DEEPL_AUTH_KEY=$configuration.apiKey
    $env:DEEPL_ENDPOINT='https://api-free.deepl.com'
    & python (Join-Path $PSScriptRoot 'verify_translation_provider.py')
    if($LASTEXITCODE -ne 0){throw 'Live translation verification failed; no release criterion was approved'}
} finally {
    [Array]::Clear($bytes,0,$bytes.Length);$configuration=$null
    if($null -eq $oldKey){[Environment]::SetEnvironmentVariable('DEEPL_AUTH_KEY',[NullString]::Value,'Process')}else{$env:DEEPL_AUTH_KEY=$oldKey}
    if($null -eq $oldEndpoint){[Environment]::SetEnvironmentVariable('DEEPL_ENDPOINT',[NullString]::Value,'Process')}else{$env:DEEPL_ENDPOINT=$oldEndpoint}
}
