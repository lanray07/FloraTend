param([switch]$SelfTest)
$ErrorActionPreference='Stop'

function Protect-PlantNetConfiguration([string]$json) {
    $bytes=[Text.Encoding]::UTF8.GetBytes($json)
    try { return [Security.Cryptography.ProtectedData]::Protect($bytes,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser) }
    finally { [Array]::Clear($bytes,0,$bytes.Length) }
}

if($SelfTest) {
    $sample='private-dpapi-roundtrip-check'
    $cipher=Protect-PlantNetConfiguration $sample
    $decoded=[Security.Cryptography.ProtectedData]::Unprotect($cipher,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser)
    try {
        if([Text.Encoding]::UTF8.GetString($decoded) -ne $sample){throw 'DPAPI round-trip failed'}
        if([Text.Encoding]::UTF8.GetString($cipher).Contains($sample)){throw 'Plaintext appeared in protected bytes'}
        @{dpapiRoundTrip=$true;plaintextFileWritten=$false;protection='CurrentUser';networkRequestMade=$false}|ConvertTo-Json
    } finally { [Array]::Clear($decoded,0,$decoded.Length) }
    exit
}

$root=Join-Path $env:USERPROFILE '.android'
[IO.Directory]::CreateDirectory($root)|Out-Null
$destination=Join-Path $root 'floratend-plantnet-secrets.dpapi'
$secure=Read-Host 'Enter the API key from your existing/free Pl@ntNet account (hidden)' -AsSecureString
$pointer=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $apiKey=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    if([string]::IsNullOrWhiteSpace($apiKey) -or $apiKey.Length -lt 8 -or $apiKey.Length -gt 512 -or $apiKey -match '\s') {throw 'The key must contain 8–512 characters and no whitespace; no value was saved'}
    $salt=[byte[]]::new(32);[Security.Cryptography.RandomNumberGenerator]::Fill($salt)
    $configuration=@{provider='plantnet';plan='free';apiKey=$apiKey;rateLimitSecret=[Convert]::ToBase64String($salt);dailyAppCap=480;createdAtUtc=[DateTime]::UtcNow.ToString('o')}
    $cipher=Protect-PlantNetConfiguration ($configuration|ConvertTo-Json -Compress)
    $temporary=$destination+'.new'
    [IO.File]::WriteAllBytes($temporary,$cipher)
    $checkBytes=[Security.Cryptography.ProtectedData]::Unprotect([IO.File]::ReadAllBytes($temporary),$null,[Security.Cryptography.DataProtectionScope]::CurrentUser)
    try {
        $check=[Text.Encoding]::UTF8.GetString($checkBytes)|ConvertFrom-Json
        if($check.apiKey -cne $apiKey){throw 'Encrypted key verification failed'}
    } finally { [Array]::Clear($checkBytes,0,$checkBytes.Length);$check=$null }
    [IO.File]::Move($temporary,$destination,$true)
    Write-Output 'Saved and verified the encrypted Pl@ntNet configuration in your private .android directory. No network request was made and no key value was printed.'
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    $secure.Dispose();$apiKey=$null;$configuration=$null
    if($salt){[Array]::Clear($salt,0,$salt.Length)}
}
