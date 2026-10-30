param([switch]$SkipBuild, [string]$CandidateApk = 'app/build/outputs/apk/release/app-release.apk')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$adb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$evidence = Join-Path $projectRoot 'evidence/direct-release'
[System.IO.Directory]::CreateDirectory($evidence) | Out-Null
function Invoke-Adb([string[]]$Arguments) {
    $output = & $adb @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($output -join "`n") }
    return $output
}
if (!$SkipBuild) { & "$PSScriptRoot/build_signed_release.ps1" -BuildDeviceTests }
# Never automatically remove the app to resolve a signing mismatch: preserve user records.
Invoke-Adb @('install','-r',$CandidateApk) | Set-Content "$evidence/install.txt"
# This package contains only the instrumentation harness, never user plant data.
& $adb uninstall com.floratend.app.test 2>&1 | Out-Null
Invoke-Adb @('install','app/build/outputs/apk/androidTest/release/app-release-androidTest.apk') | Set-Content "$evidence/test-install.txt"
$stayAwake = (Invoke-Adb @('shell','settings','get','global','stay_on_while_plugged_in') | Out-String).Trim()
try {
    Invoke-Adb @('shell','settings','put','global','stay_on_while_plugged_in','7') | Out-Null
    Invoke-Adb @('shell','input','keyevent','KEYCODE_WAKEUP') | Out-Null
    Invoke-Adb @('shell','wm','dismiss-keyguard') | Out-Null
    # Privacy test first: it refuses to run against a nonempty collection/photo directory.
    $classes = 'PrivacyReleaseTest,PersistenceTest,ReminderWorkerTest,SpeechCapabilityTest,UiSmokeTest,BillingStartupTest,EntitlementStoreTest,ReleaseDeviceTest'.Split(',') | ForEach-Object { "com.floratend.app.$_" }
    $result = Invoke-Adb @('shell','am','instrument','-w','-e','class',($classes -join ','),'com.floratend.app.test/androidx.test.runner.AndroidJUnitRunner')
    $result | Set-Content "$evidence/native-tests.txt"
    Invoke-Adb @('pull','/sdcard/Android/data/com.floratend.app/files/release-device/.',"$evidence/") | Out-Null
    Invoke-Adb @('pull','/sdcard/Android/data/com.floratend.app/files/acceptance/.',"$evidence/screens/") | Out-Null
    Invoke-Adb @('pull','/sdcard/Android/data/com.floratend.app/files/release-checks/privacy-deletion.json',"$evidence/privacy-deletion.json") | Out-Null
    if (($result -join "`n") -notmatch 'OK \(15 tests\)') { throw 'Signed release device checks failed; see native-tests.txt' }
} finally {
    if ($stayAwake -and $stayAwake -ne 'null') { Invoke-Adb @('shell','settings','put','global','stay_on_while_plugged_in',$stayAwake) | Out-Null }
    else { Invoke-Adb @('shell','settings','delete','global','stay_on_while_plugged_in') | Out-Null }
}
Write-Output '15 signed-release device tests passed. Read store-probe.json for actual store availability; this does not assert a production purchase.'
