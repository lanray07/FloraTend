param([switch]$SelfTest,[ValidateSet('free','developer')][string]$Plan='free')
$ErrorActionPreference='Stop'

function Protect-TranslationConfiguration([string]$json) {
    $bytes=[Text.Encoding]::UTF8.GetBytes($json)
    try { return [Security.Cryptography.ProtectedData]::Protect($bytes,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser) }
    finally { [Array]::Clear($bytes,0,$bytes.Length) }
}

if($SelfTest) {
    $sample='translation-dpapi-test-value'
    $cipher=Protect-TranslationConfiguration $sample
    $decoded=[Security.Cryptography.ProtectedData]::Unprotect($cipher,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser)
    try {
        if([Text.Encoding]::UTF8.GetString($decoded) -ne $sample){throw 'DPAPI round-trip failed'}
        if([Text.Encoding]::UTF8.GetString($cipher).Contains($sample)){throw 'Protected bytes contain plaintext'}
        @{dpapiRoundTrip=$true;protection='CurrentUser';plaintextFileWritten=$false;networkRequestMade=$false}|ConvertTo-Json
    } finally { [Array]::Clear($decoded,0,$decoded.Length) }
    exit
}

$privateRoot=Join-Path $env:USERPROFILE '.android'
[IO.Directory]::CreateDirectory($privateRoot)|Out-Null
$destination=Join-Path $privateRoot 'floratend-deepl-secrets.dpapi'
$secure=Read-Host "Enter an existing DeepL API $Plan key (hidden; not the Pl@ntNet key)" -AsSecureString
$pointer=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $apiKey=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    if([string]::IsNullOrWhiteSpace($apiKey) -or $apiKey.Length -lt 8 -or $apiKey.Length -gt 512 -or $apiKey -match '\s') {throw 'Key must contain8–512characters with no whitespace; nothing saved'}
    # Both allowed plans are free. Keep the Free endpoint guard: a Developer
    # account that rejects this endpoint must fail closed, never retry a paid one.
    $configuration=@{provider='deepl';plan=$Plan;apiKey=$apiKey;endpoint='https://api-free.deepl.com';createdAtUtc=[DateTime]::UtcNow.ToString('o')}
    $cipher=Protect-TranslationConfiguration ($configuration|ConvertTo-Json -Compress)
    $temporary=$destination+'.new'
    [IO.File]::WriteAllBytes($temporary,$cipher)
    $checkBytes=[Security.Cryptography.ProtectedData]::Unprotect([IO.File]::ReadAllBytes($temporary),$null,[Security.Cryptography.DataProtectionScope]::CurrentUser)
    try {
        $check=[Text.Encoding]::UTF8.GetString($checkBytes)|ConvertFrom-Json
        if($check.apiKey -cne $apiKey -or $check.plan -ne $Plan){throw 'Protected key verification failed'}
    } finally { [Array]::Clear($checkBytes,0,$checkBytes.Length);$check=$null }
    [IO.File]::Move($temporary,$destination,$true)
    Write-Output "Saved and verified encrypted DeepL API $Plan configuration privately. No key printed, no network request made. Live endpoint compatibility still needs verification. Tell Codex it is saved."
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    $secure.Dispose();$apiKey=$null;$configuration=$null
}
