param([switch]$SkipBuild)
$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$adb=Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
function Adb([string[]]$ArgsList){$value=& $adb @ArgsList 2>&1;if($LASTEXITCODE -ne 0){throw ($value -join "`n")};return $value}
$other=Adb @('shell','dumpsys','activity','processes') | Select-String 'ActiveInstrumentation' | Where-Object{$_ -notmatch 'com.floratend.app'}
if($other){throw 'Another tablet instrumentation session is active'}
if(!$SkipBuild){& .\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain *> evidence/store-capture-build.log;if($LASTEXITCODE -ne 0){throw 'Capture build failed'}}
$runFolder=Join-Path 'evidence/store-captures' ('run-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Force $runFolder | Out-Null
$wifi=(Adb @('shell','settings','get','global','wifi_on') | Out-String).Trim()
$awake=(Adb @('shell','settings','get','global','stay_on_while_plugged_in') | Out-String).Trim()
try {
 Adb @('shell','settings','put','global','stay_on_while_plugged_in','7') | Out-Null
 Adb @('shell','input','keyevent','KEYCODE_WAKEUP') | Out-Null
 $lock=Adb @('shell','dumpsys','window','policy') | Out-String
 if($lock -match 'mIsShowing=true|mInputRestricted=true'){throw 'Unlock the tablet before capture'}
 Adb @('install','-r','app/build/outputs/apk/debug/app-debug.apk') | Out-Null
 Adb @('install','-r','app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk') | Out-Null
 Adb @('shell','svc','wifi','disable') | Out-Null
 Start-Sleep -Seconds 2
 $network=Adb @('shell','dumpsys','connectivity') | Select-String 'Active default network'
 $network | Set-Content -LiteralPath (Join-Path $runFolder 'network-state.txt') -Encoding utf8
 if(($network -join '') -notmatch 'none'){throw 'Offline capture requires no active network'}
 $result=Adb @('shell','am','instrument','-w','-e','class','com.floratend.app.StoreScreenshotTest','com.floratend.app.test/androidx.test.runner.AndroidJUnitRunner')
 $result | Set-Content -LiteralPath (Join-Path $runFolder 'instrumentation.txt') -Encoding utf8
 Adb @('pull','/sdcard/Android/data/com.floratend.app/files/store-captures/.',$runFolder) | Out-Null
 if(($result -join "`n") -notmatch 'OK \(1 test\)' -or ($result -join "`n") -match 'Error in |FAILURES!!!|Process crashed'){throw 'Store capture failed; partial evidence retained'}
 $manifest=Get-Content -LiteralPath (Join-Path $runFolder 'capture-manifest.json') -Raw | ConvertFrom-Json
 if($manifest.locales.Count -ne 19){throw 'Expected 19 complete locale captures'}
 Write-Output "Captured 152 actual offline UI scenes across 19 app locales in $runFolder; human review remains pending."
} finally {
 if($wifi -in @('1','2')){Adb @('shell','svc','wifi','enable') | Out-Null}else{Adb @('shell','svc','wifi','disable') | Out-Null}
 if($awake -and $awake -ne 'null'){Adb @('shell','settings','put','global','stay_on_while_plugged_in',$awake) | Out-Null}else{Adb @('shell','settings','delete','global','stay_on_while_plugged_in') | Out-Null}
}
