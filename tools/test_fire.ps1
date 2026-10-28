param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (!(Test-Path -LiteralPath $adb)) { throw 'Android platform-tools not found' }
New-Item -ItemType Directory -Force evidence/device | Out-Null
function Invoke-Adb([string[]]$Arguments) {
    $result = & $adb @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($result -join "`n") }
    return $result
}
function Run-Suite([string]$Name, [string]$Class = '') {
    $argsForTest = @('shell','am','instrument','-w')
    if ($Class) { $argsForTest += @('-e','class',$Class) }
    $argsForTest += 'com.floratend.app.test/androidx.test.runner.AndroidJUnitRunner'
    $result = Invoke-Adb $argsForTest
    $result | Set-Content -Encoding utf8 "evidence/device/$Name.txt"
    Invoke-Adb @('pull','/sdcard/Android/data/com.floratend.app/files/acceptance/.',"evidence/device/$Name/") | Out-Null
    if (($result -join "`n") -notmatch 'OK \(\d+ tests?\)') { throw "Device suite failed: $Name" }
}
if (!$SkipBuild) {
    & .\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug --console=plain *> evidence/device-build.log
    if ($LASTEXITCODE -ne 0) { throw 'Gradle validation failed; see evidence/device-build.log' }
}
Invoke-Adb @('install','-r','app/build/outputs/apk/debug/app-debug.apk') | Out-Null
Invoke-Adb @('install','-r','app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk') | Out-Null
$fontScale = (Invoke-Adb @('shell','settings','get','system','font_scale') | Out-String).Trim()
$wifi = (Invoke-Adb @('shell','settings','get','global','wifi_on') | Out-String).Trim()
$originalSize = (Invoke-Adb @('shell','wm','size') | Out-String).Trim()
$originalDensity = (Invoke-Adb @('shell','wm','density') | Out-String).Trim()
if ($originalSize -match 'Override' -or $originalDensity -match 'Override') { throw 'Pre-existing display overrides found; preserve and test them manually' }
try {
    Invoke-Adb @('shell','input','keyevent','KEYCODE_WAKEUP') | Out-Null
    Invoke-Adb @('shell','wm','dismiss-keyguard') | Out-Null
    Run-Suite 'physical-hd10'
    Invoke-Adb @('shell','wm','size','600x1024') | Out-Null
    Invoke-Adb @('shell','wm','density','160') | Out-Null
    Invoke-Adb @('shell','settings','put','system','font_scale','1.3') | Out-Null
    Run-Suite 'layout-7in-large-font' 'com.floratend.app.UiSmokeTest'
    Invoke-Adb @('shell','wm','size','800x1280') | Out-Null
    Invoke-Adb @('shell','settings','put','system','font_scale','1.0') | Out-Null
    Run-Suite 'layout-8in' 'com.floratend.app.UiSmokeTest'
    Invoke-Adb @('shell','wm','size','reset') | Out-Null
    Invoke-Adb @('shell','wm','density','reset') | Out-Null
    Invoke-Adb @('shell','svc','wifi','disable') | Out-Null
    Start-Sleep -Seconds 2
    $offline = Invoke-Adb @('shell','dumpsys','connectivity') | Select-String 'Active default network'
    $offline | Set-Content -Encoding utf8 evidence/device/offline-network-state.txt
    if (($offline -join '') -notmatch 'none') { throw 'No-network condition could not be verified' }
    Run-Suite 'offline-hd10' 'com.floratend.app.UiSmokeTest'
} finally {
    Invoke-Adb @('shell','wm','size','reset') | Out-Null
    Invoke-Adb @('shell','wm','density','reset') | Out-Null
    if ($fontScale -and $fontScale -ne 'null') { Invoke-Adb @('shell','settings','put','system','font_scale',$fontScale) | Out-Null }
    else { Invoke-Adb @('shell','settings','delete','system','font_scale') | Out-Null }
    if ($wifi -eq '1' -or $wifi -eq '2') { Invoke-Adb @('shell','svc','wifi','enable') | Out-Null }
    else { Invoke-Adb @('shell','svc','wifi','disable') | Out-Null }
}
Write-Output 'Physical HD10, 7/8-inch layout overrides, large-font and offline suites passed. Overrides are not physical 7/8-inch device verification.'
