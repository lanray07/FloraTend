param()
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$adb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$evidence = Join-Path $projectRoot 'evidence/lat-billing'
[System.IO.Directory]::CreateDirectory($evidence) | Out-Null
function Invoke-Adb([string[]]$Arguments) {
    $output = & $adb @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($output -join "`n") }
    return $output
}
$testApk = 'app/build/outputs/apk/androidTest/release/app-release-androidTest.apk'
if (!(Test-Path -LiteralPath $testApk)) { throw 'Build the signed release test harness with tools/build_signed_release.ps1 -BuildDeviceTests first.' }
$package = Invoke-Adb @('shell','dumpsys','package','com.floratend.app')
if (($package -join "`n") -match 'pkgFlags=.*DEBUGGABLE') { throw 'This test requires the actual non-debuggable release installation.' }
$sandbox = (Invoke-Adb @('shell','getprop','debug.amazon.sandboxmode') | Out-String).Trim()
if ($sandbox -and $sandbox -ne 'none') { throw 'Disable App Tester sandbox mode before production-service verification.' }
try {
    Invoke-Adb @('install','-r',$testApk) | Set-Content "$evidence/harness-install.txt"
    Invoke-Adb @('shell','input','keyevent','KEYCODE_WAKEUP') | Out-Null
    Invoke-Adb @('shell','wm','dismiss-keyguard') | Out-Null
    # Remove only this probe's generated report so an old result cannot look current.
    Invoke-Adb @('shell','rm','-f','/sdcard/Android/data/com.floratend.app/files/lat-billing/production-callbacks.json') | Out-Null
    $result = Invoke-Adb @('shell','am','instrument','-w','-e','class','com.floratend.app.LatBillingTest#actualProductionCatalogueAndClientPrice','com.floratend.app.test/androidx.test.runner.AndroidJUnitRunner')
    $result | Set-Content "$evidence/catalogue-test.txt"
    Invoke-Adb @('pull','/sdcard/Android/data/com.floratend.app/files/lat-billing/production-callbacks.json',"$evidence/production-callbacks.json") | Out-Null
    if (($result -join "`n") -notmatch 'OK \(1 test\)') { throw 'Live catalogue gate failed. Read production-callbacks.json; do not treat this as a successful purchase test.' }
} finally {
    # The harness package contains no user plant records. Keep the app installed.
    & $adb uninstall com.floratend.app.test 2>&1 | Out-Null
    & $adb shell am start -n com.floratend.app/.MainActivity 2>&1 | Out-Null
}
Write-Output 'Actual production catalogue and client price verified; purchase and owned-entitlement restore still need their separate checks.'
