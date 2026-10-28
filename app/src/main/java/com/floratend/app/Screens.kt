package com.floratend.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable fun CareScreen(plants:List<Plant>,schedules:List<CareSchedule>,vm:PlantViewModel,onAdd:()->Unit,onComplete:(CareSchedule)->Unit) {
    var view by remember { mutableIntStateOf(0) };var filter by remember { mutableStateOf("") };var type by remember { mutableStateOf("") };var reschedule by remember { mutableStateOf<CareSchedule?>(null) };var newDate by remember { mutableStateOf("") }
    Heading(s(R.string.care_calendar),s(R.string.check_first))
    Button(onClick=onAdd,enabled=plants.isNotEmpty()){Text(s(R.string.add_schedule))}
    Row { listOf(R.string.day,R.string.week,R.string.month).forEachIndexed { i,id -> FilterChip(selected=view==i,onClick={view=i},label={Text(s(id))},modifier=Modifier.padding(end=8.dp)) } }
    Row(Modifier.horizontalScroll(rememberScrollState())) { FilterChip(selected=filter.isEmpty(),onClick={filter=""},label={Text(s(R.string.all))});plants.forEach { p -> FilterChip(selected=filter==p.id,onClick={filter=p.id},label={Text(p.name)}) } }
    Row(Modifier.horizontalScroll(rememberScrollState())) { FilterChip(selected=type.isEmpty(),onClick={type=""},label={Text(s(R.string.all))});listOf("soil","water","fertilise","prune","repot","clean","pest","custom").forEach { key -> FilterChip(selected=type==key,onClick={type=key},label={Text(careLabel(key))}) } }
    val until=LocalDate.now().plusDays(when(view){0->0;1->7;else->30}).toString()
    val visible=schedules.filter { (filter.isEmpty() || it.plantId==filter) && (type.isEmpty() || it.type==type) && it.nextDate<=until }
    if(visible.isEmpty()) Empty(s(R.string.no_tasks),s(R.string.no_tasks_body))
    LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)) { items(visible,key={it.id}) { schedule -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(plants.find { it.id==schedule.plantId }?.name.orEmpty(),style=MaterialTheme.typography.titleLarge);Text(careLabel(schedule.type));Text(formatDate(schedule.nextDate));if(schedule.paused) Text(s(R.string.paused));FlowRow(horizontalArrangement=Arrangement.spacedBy(4.dp)) {TextButton(enabled=!schedule.paused,onClick={onComplete(schedule)}){Text(s(R.string.record_care))};TextButton(onClick={vm.schedule(schedule.copy(nextDate=LocalDate.now().plusDays(1).toString()))}){Text(s(R.string.snooze))};TextButton(onClick={vm.schedule(schedule.copy(paused=!schedule.paused))}){Text(s(if(schedule.paused) R.string.resume else R.string.pause))};TextButton(onClick={newDate=schedule.nextDate;reschedule=schedule}){Text(s(R.string.reschedule))}} } } } }
    reschedule?.let { schedule -> AlertDialog(onDismissRequest={reschedule=null},title={Text(s(R.string.reschedule))},text={Field(newDate,R.string.date_iso,{newDate=it})},confirmButton={TextButton(enabled=runCatching { LocalDate.parse(newDate) }.isSuccess,onClick={vm.schedule(schedule.copy(nextDate=newDate));reschedule=null}){Text(s(R.string.save))}},dismissButton={TextButton(onClick={reschedule=null}){Text(s(R.string.cancel))}}) }
}

@Composable fun VoiceScreen(activity:MainActivity,plants:List<Plant>,schedules:List<CareSchedule>,vm:PlantViewModel,onTab:(Int)->Unit,onAdd:()->Unit,onSearch:(String)->Unit) {
    val focusManager=LocalFocusManager.current
    var response by remember { mutableStateOf("") };var pending by remember { mutableStateOf<CareSchedule?>(null) }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if(granted) activity.listen() else response=activity.getString(R.string.mic_denied) }
    Heading(s(R.string.voice_assistant),s(R.string.voice_intro))
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(s(R.string.voice_privacy));if(!SpeechRecognizer.isRecognitionAvailable(activity)) Text(s(R.string.voice_unavailable),modifier=Modifier.padding(vertical=16.dp))
        Button(onClick={if(ContextCompat.checkSelfPermission(activity,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) activity.listen() else permission.launch(Manifest.permission.RECORD_AUDIO)},enabled=!activity.listening && SpeechRecognizer.isRecognitionAvailable(activity)) {Icon(FloraIcons.Mic,null);Text(s(if(activity.listening) R.string.listening else R.string.push_to_talk))}
        Field(activity.transcript,R.string.command,{activity.transcript=it},3)
        Button(onClick={
            focusManager.clearFocus()
            response=when(val command=VoiceParser.parse(activity.transcript)) {
                VoiceCommand.Add->{onAdd();activity.getString(R.string.add_plant)}
                VoiceCommand.Calendar->{onTab(1);activity.getString(R.string.care_calendar)}
                VoiceCommand.Due,VoiceCommand.Read->{val due=schedules.filter { !it.paused && it.nextDate<=LocalDate.now().toString() };activity.resources.getQuantityString(R.plurals.reminder_count,due.size,due.size)+" "+due.mapNotNull { schedule -> plants.find { it.id==schedule.plantId }?.name }.distinct().joinToString(", ")}
                is VoiceCommand.Location->{onSearch(command.name);activity.getString(R.string.garden)}
                is VoiceCommand.Water->{val matches=plants.filter { it.name.equals(command.name,true) };val schedule=if(matches.size==1) schedules.singleOrNull { it.plantId==matches[0].id && it.type in listOf("soil","water") && !it.paused } else null;if(schedule!=null){pending=schedule;activity.getString(R.string.confirm_water)}else activity.getString(R.string.voice_ambiguous)}
                VoiceCommand.Unknown->activity.getString(R.string.voice_unknown)
            };activity.speak(response)
        }){Text(s(R.string.run_command))}
        Text(response,modifier=Modifier.padding(vertical=16.dp));Text(s(R.string.voice_examples))
    }
    pending?.let { schedule -> AlertDialog(onDismissRequest={pending=null},title={Text(s(R.string.confirm_water))},text={Text(s(R.string.check_first))},confirmButton={TextButton(onClick={vm.complete(schedule,activity.getString(R.string.watered));pending=null;response=activity.getString(R.string.care_saved);activity.speak(response)}){Text(s(R.string.confirm))}},dismissButton={TextButton(onClick={pending=null}){Text(s(R.string.cancel))}}) }
}

@Composable fun SettingsScreen(app:FloraApp,vm:PlantViewModel) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    val dark by app.settings.dark.collectAsState(false);val imperial by app.settings.imperial.collectAsState(false);val reminders by app.settings.reminders.collectAsState(false);val premium by vm.premium.collectAsState()
    val price by app.billing.price.collectAsState();val message by app.billing.message.collectAsState();val available by app.billing.available.collectAsState()
    var wipe by remember { mutableStateOf(false) };var status by remember { mutableStateOf("") };var payload by remember { mutableStateOf("") }
    val exporter=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> if(uri!=null) scope.launch { runCatching { withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(payload.toByteArray(Charsets.UTF_8)) } ?: error("Unavailable") } }.onSuccess { status=context.getString(R.string.export_saved) }.onFailure { status=context.getString(R.string.export_error) } } }
    val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> scope.launch { app.settings.set("reminders",granted);Reminders.refresh(app) } }
    Heading(s(R.string.settings),s(R.string.settings_intro))
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Toggle(s(R.string.dark_theme),dark){scope.launch{app.settings.set("dark",it)}}
        Toggle(s(R.string.imperial),imperial){scope.launch{app.settings.set("imperial",it)}}
        Toggle(s(R.string.care_reminders),reminders){if(it && Build.VERSION.SDK_INT>=33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else scope.launch{app.settings.set("reminders",it);Reminders.refresh(app)}}
        HorizontalDivider();Text(s(R.string.premium),style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(top=20.dp));Text(s(if(premium) R.string.premium_active else R.string.premium_intro))
        Button(onClick={app.billing.purchase()},enabled=available && !premium){Text(price?.let { s(R.string.unlock_price).format(it) } ?: s(R.string.store_unavailable))}
        TextButton(onClick={app.billing.restore()}){Text(s(R.string.restore))};if(message.isNotEmpty()) Text(message)
        Text(s(R.string.export_notice));Row{TextButton(enabled=premium,onClick={payload=Export.json(vm.plants.value,vm.schedules.value,vm.journal.value,vm.history.value);exporter.launch("floratend.json")}){Text(s(R.string.export_json))};TextButton(enabled=premium,onClick={payload=Export.csv(vm.plants.value);exporter.launch("floratend.csv")}){Text(s(R.string.export_csv))}}
        Text(status)
        Text(s(R.string.identification_unavailable),modifier=Modifier.padding(vertical=16.dp));Text(s(R.string.language_pending));Text(s(R.string.privacy_intro),modifier=Modifier.padding(vertical=16.dp));Text(s(R.string.backup_unavailable));TextButton(onClick={wipe=true}){Text(s(R.string.delete_all))}
    }
    if(wipe) Confirm(s(R.string.delete_all),{wipe=false}){vm.wipe();wipe=false}
}
@Composable fun Toggle(label:String,value:Boolean,change:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text(label,Modifier.weight(1f));Switch(value,change,modifier=Modifier.semantics { contentDescription=label })}}
