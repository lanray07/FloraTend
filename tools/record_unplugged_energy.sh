#!/system/bin/sh
trap '' HUP
BASE=${0%/*}
echo $$ > "$BASE/logger.pid"
echo WAITING_FOR_PHYSICAL_UNPLUG > "$BASE/status.txt"
now() { read raw rest < /proc/uptime; echo "${raw%%.*}"; }
snapshot() {
 echo "@@SAMPLE $1 $(date +%s) $(now) $2" >> "$BASE/battery-samples.txt"
 dumpsys battery >> "$BASE/battery-samples.txt"
 echo "@@SCREEN $1" >> "$BASE/battery-samples.txt"
 dumpsys power | grep 'mWakefulness=' >> "$BASE/battery-samples.txt"
}
powered() { dumpsys battery | grep -E '(AC|USB|Wireless) powered: true' >/dev/null; }
stopped() { dumpsys battery | grep -i 'updates stopped' >/dev/null; }
if stopped; then echo ABORTED_SIMULATED_BATTERY_STATE > "$BASE/status.txt";exit 3;fi
snapshot 0 waiting
waited=0
while powered; do
 sleep 5
 waited=$((waited+5))
 if [ $((waited%60)) -eq 0 ];then snapshot "$waited" waiting;fi
 if [ "$waited" -ge 3600 ];then echo EXPIRED_WAITING_FOR_UNPLUG > "$BASE/status.txt";exit 4;fi
done
# Put the foreground app in the background normally; never force-stop the target.
input keyevent 3
input keyevent 223
sleep 5
if powered || stopped;then echo ABORTED_POWER_RECONNECTED_BEFORE_START > "$BASE/status.txt";exit 5;fi
start=$(now)
echo "$start" > "$BASE/start-uptime.txt"
echo MEASURING_PHYSICALLY_UNPLUGGED > "$BASE/status.txt"
dumpsys batterystats --charged com.floratend.app > "$BASE/app-batterystats-start.txt"
pidof com.floratend.app > "$BASE/app-pid-start.txt"
snapshot 1 start
index=2
while true;do
 sleep 60
 if powered || stopped;then
  snapshot "$index" invalid
  echo ABORTED_POWER_RECONNECTED_DURING_WINDOW > "$BASE/status.txt"
  exit 6
 fi
 elapsed=$(($(now)-start))
 snapshot "$index" measurement
 if [ "$elapsed" -ge 1800 ];then break;fi
 index=$((index+1))
done
dumpsys batterystats --charged com.floratend.app > "$BASE/app-batterystats-end.txt"
pidof com.floratend.app > "$BASE/app-pid-end.txt"
echo "$elapsed" > "$BASE/elapsed-seconds.txt"
echo COMPLETE_30_MINUTES_PHYSICALLY_UNPLUGGED > "$BASE/status.txt"
