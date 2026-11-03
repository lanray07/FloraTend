$ErrorActionPreference='Stop'
$privatePath=Join-Path $env:USERPROFILE '.android/floratend-deepl-secrets.dpapi'
if(!(Test-Path -LiteralPath $privatePath)){throw 'Private DeepL free-plan configuration is missing; use configure_deepl_key.ps1 locally, with -Plan developer for an API Developer account. Do not paste a key into chat.'}
$bytes=[Security.Cryptography.ProtectedData]::Unprotect([IO.File]::ReadAllBytes($privatePath),$null,[Security.Cryptography.DataProtectionScope]::CurrentUser)
$oldKey=$env:DEEPL_AUTH_KEY
$oldEndpoint=$env:DEEPL_ENDPOINT
$oldPlan=$env:DEEPL_PLAN
try {
    $configuration=[Text.Encoding]::UTF8.GetString($bytes)|ConvertFrom-Json
    if($configuration.provider -ne 'deepl' -or $configuration.plan -notin @('free','developer') -or $configuration.endpoint -ne 'https://api-free.deepl.com' -or [string]::IsNullOrWhiteSpace($configuration.apiKey)){throw 'Expected private free-plan configuration using the Free endpoint'}
    $env:DEEPL_AUTH_KEY=$configuration.apiKey
    $env:DEEPL_ENDPOINT='https://api-free.deepl.com'
    $env:DEEPL_PLAN=$configuration.plan
    & python (Join-Path $PSScriptRoot 'verify_translation_provider.py')
    if($LASTEXITCODE -ne 0){throw 'Live translation verification failed; no release criterion was approved'}
} finally {
    [Array]::Clear($bytes,0,$bytes.Length);$configuration=$null
    if($null -eq $oldKey){[Environment]::SetEnvironmentVariable('DEEPL_AUTH_KEY',[NullString]::Value,'Process')}else{$env:DEEPL_AUTH_KEY=$oldKey}
    if($null -eq $oldEndpoint){[Environment]::SetEnvironmentVariable('DEEPL_ENDPOINT',[NullString]::Value,'Process')}else{$env:DEEPL_ENDPOINT=$oldEndpoint}
    if($null -eq $oldPlan){[Environment]::SetEnvironmentVariable('DEEPL_PLAN',[NullString]::Value,'Process')}else{$env:DEEPL_PLAN=$oldPlan}
}
