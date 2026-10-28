param([switch]$SkipBuild)
$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$adb=Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
New-Item -ItemType Directory -Force evidence/localized-device | Out-Null
$other=& $adb shell dumpsys activity processes | Select-String 'ActiveInstrumentation' | Where-Object { $_ -notmatch 'com.floratend.app' }
if ($other) { throw 'Another device test is active. Reserve the tablet before running this suite.' }
if (!$SkipBuild) {
    & .\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --console=plain *> evidence/localized-build-final.log
    if ($LASTEXITCODE -ne 0) { throw 'Build checks failed' }
}
$original=(& $adb shell settings get global stay_on_while_plugged_in | Out-String).Trim()
try {
    & $adb shell settings put global stay_on_while_plugged_in 7
    & $adb shell input keyevent KEYCODE_WAKEUP
    & $adb shell wm dismiss-keyguard
    $lockState=(& $adb shell dumpsys window policy | Out-String)
    if ($lockState -match 'showingAndNotOccluded=true') {
        throw 'Tablet is locked. Unlock it on the device before running UI tests; this runner does not bypass a PIN.'
    }
    & $adb install -r app/build/outputs/apk/debug/app-debug.apk
    if ($LASTEXITCODE -ne 0) { throw 'App installation failed' }
    & $adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
    if ($LASTEXITCODE -ne 0) { throw 'Test installation failed' }
    $classes='com.floratend.app.PersistenceTest,com.floratend.app.ReminderWorkerTest,com.floratend.app.SpeechCapabilityTest,com.floratend.app.UiSmokeTest,com.floratend.app.LocalizedUiTest'
    $result=& $adb shell am instrument -w -e class $classes com.floratend.app.test/androidx.test.runner.AndroidJUnitRunner
    if (Test-Path evidence/localized-device/instrumentation.txt) {
        Copy-Item -LiteralPath evidence/localized-device/instrumentation.txt -Destination (Join-Path 'evidence/localized-device' ('instrumentation-previous-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'.txt'))
    }
    $result | Set-Content -Encoding utf8 evidence/localized-device/instrumentation.txt
    & $adb pull /sdcard/Android/data/com.floratend.app/files/localization/. evidence/localized-device/screens/
    if (($result -join "`n") -notmatch 'OK \(11 tests\)' -or ($result -join "`n") -match 'FAILURES!!!|INSTRUMENTATION_FAILED|Process crashed') { throw 'Localized device suite failed; inspect retained output' }
    Write-Output 'All 11 real-device tests passed. Localized commands use typed input; human language review and actual speech recognition remain unverified.'
} finally {
    if ($original -and $original -ne 'null') { & $adb shell settings put global stay_on_while_plugged_in $original }
    else { & $adb shell settings delete global stay_on_while_plugged_in }
}
