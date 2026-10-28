package com.floratend.app

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable fun PhotoPicker(photo:String?,onPhoto:(String)->Unit) {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    var error by remember { mutableStateOf(false) }
    fun import(uri:android.net.Uri) { scope.launch { runCatching { withContext(Dispatchers.IO) { Photos.import(context,uri) } }.onSuccess(onPhoto).onFailure { error=true } } }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { uri -> import(uri) } }
    // External camera activity owns camera permission; thumbnail is copied privately.
    val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> if(bitmap!=null) scope.launch { runCatching { withContext(Dispatchers.IO) { Photos.save(context,bitmap) } }.onSuccess(onPhoto).onFailure { error=true } } }
    Photo(photo)
    Row { TextButton(onClick={picker.launch("image/*")}) { Text(s(R.string.choose_photo)) }; TextButton(onClick={runCatching { camera.launch(null) }.onFailure { error=true }}) { Text(s(R.string.take_photo)) } }
    if(error) Text(s(R.string.photo_error),color=MaterialTheme.colorScheme.error)
}
@Composable fun PlantEditor(plant:Plant,onDismiss:()->Unit,onSave:(Plant)->Unit) {
    var name by remember { mutableStateOf(plant.name) };var species by remember { mutableStateOf(plant.species) };var location by remember { mutableStateOf(plant.location) };var notes by remember { mutableStateOf(plant.notes) };var soil by remember { mutableStateOf(plant.soil) };var light by remember { mutableStateOf(plant.light) };var pot by remember { mutableStateOf(plant.potCm?.toString().orEmpty()) };var acquired by remember { mutableStateOf(plant.acquired) };var category by remember { mutableStateOf(plant.category) };var photo by remember { mutableStateOf(plant.photo) }
    val valid=name.isNotBlank() && runCatching { LocalDate.parse(acquired) }.isSuccess && (pot.isBlank() || (pot.toDoubleOrNull()?.let { it.isFinite() && it>0 } == true))
    AlertDialog(onDismissRequest=onDismiss,title={Text(s(R.string.plant_details))},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Field(name,R.string.nickname,{name=it});Field(species,R.string.species,{species=it});Field(location,R.string.location,{location=it});Field(acquired,R.string.acquired,{acquired=it});Field(pot,R.string.pot_size,{pot=it});Field(soil,R.string.soil_type,{soil=it});Field(light,R.string.light,{light=it});Field(notes,R.string.notes,{notes=it},3)
        listOf("Indoor","Outdoor","Succulent","Cactus","Herb","Flowering","Tree","Other").forEach { key -> FilterChip(selected=category==key,onClick={category=key},label={Text(categoryLabel(key))}) }; PhotoPicker(photo){photo=it}
        if(!valid) Text(s(R.string.valid_plant))
    }},confirmButton={TextButton(onClick={onSave(plant.copy(name=name.trim(),species=species.trim(),location=location.trim(),category=category,acquired=acquired,potCm=pot.toDoubleOrNull(),soil=soil,light=light,notes=notes,photo=photo))},enabled=valid){Text(s(R.string.save))}},dismissButton={TextButton(onClick=onDismiss){Text(s(R.string.cancel))}})
}
@Composable fun ScheduleEditor(plants:List<Plant>,dismiss:()->Unit,save:(CareSchedule)->Unit) {
    var plant by remember { mutableStateOf(plants.firstOrNull()?.id.orEmpty()) };var type by remember { mutableStateOf("soil") };var days by remember { mutableStateOf("7") };var date by remember { mutableStateOf(LocalDate.now().toString()) }
    val valid=plant.isNotBlank() && (days.toIntOrNull() ?: 0) in 1..3650 && runCatching { LocalDate.parse(date) }.isSuccess
    AlertDialog(onDismissRequest=dismiss,title={Text(s(R.string.add_schedule))},text={Column(Modifier.verticalScroll(rememberScrollState())) { plants.forEach { p -> FilterChip(selected=plant==p.id,onClick={plant=p.id},label={Text(p.name)}) }; listOf("soil","water","fertilise","prune","repot","clean","pest","custom").forEach { key -> FilterChip(selected=type==key,onClick={type=key},label={Text(careLabel(key))}) };Field(days,R.string.interval_days,{days=it});Field(date,R.string.date_iso,{date=it});Text(s(R.string.check_first)) }},confirmButton={TextButton(enabled=valid,onClick={save(CareSchedule(plantId=plant,type=type,intervalDays=days.toInt(),nextDate=date))}){Text(s(R.string.save))}},dismissButton={TextButton(onClick=dismiss){Text(s(R.string.cancel))}})
}
@Composable fun JournalEditor(plants:List<Plant>,entry:JournalEntry?,dismiss:()->Unit,save:(JournalEntry)->Unit,delete:()->Unit) {
    val context=LocalContext.current
    val app=context.applicationContext as FloraApp
    val imperial by app.settings.imperial.collectAsState(false)
    var plant by remember { mutableStateOf(entry?.plantId ?: plants.firstOrNull()?.id.orEmpty()) };var note by remember { mutableStateOf(entry?.note.orEmpty()) };var height by remember(entry?.id,imperial) { mutableStateOf(entry?.heightCm?.let { (if(imperial) it/2.54 else it).toString() }.orEmpty()) };var leaves by remember { mutableStateOf(entry?.leaves?.toString().orEmpty()) };var date by remember { mutableStateOf(entry?.date ?: LocalDate.now().toString()) };var photo by remember { mutableStateOf(entry?.photo) };var confirm by remember { mutableStateOf(false) }
    val valid=plant.isNotBlank() && (note.isNotBlank() || photo!=null || height.isNotBlank() || leaves.isNotBlank()) && runCatching { LocalDate.parse(date) }.isSuccess && (height.isBlank() || height.toDoubleOrNull()?.let { it.isFinite() && it>=0 }==true) && (leaves.isBlank() || (leaves.toIntOrNull() ?: -1)>=0)
    AlertDialog(onDismissRequest=dismiss,title={Text(s(R.string.growth_journal))},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) { plants.forEach { p -> FilterChip(selected=plant==p.id,onClick={plant=p.id},label={Text(p.name)}) };Field(date,R.string.date_iso,{date=it});Field(note,R.string.notes,{note=it},3);Field(height,if(imperial) R.string.height_in else R.string.height_cm,{height=it});Field(leaves,R.string.leaf_count,{leaves=it});PhotoPicker(photo){photo=it};if(entry!=null) TextButton(onClick={confirm=true}) {Text(s(R.string.delete))} }},confirmButton={TextButton(enabled=valid,onClick={save((entry ?: JournalEntry(plantId=plant,note=note)).copy(plantId=plant,note=note,date=date,heightCm=height.toDoubleOrNull()?.times(if(imperial) 2.54 else 1.0),leaves=leaves.toIntOrNull(),photo=photo))}) {Text(s(R.string.save))}},dismissButton={TextButton(onClick=dismiss){Text(s(R.string.cancel))}})
    if(confirm) Confirm(s(R.string.delete_entry),{confirm=false},delete)
}
@Composable fun Onboarding(done:()->Unit) {
    var step by remember { mutableIntStateOf(0) };var experience by remember { mutableIntStateOf(0) };var outdoors by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),verticalArrangement=Arrangement.Center) {
        Image(painterResource(R.drawable.botanical_hero),contentDescription=null,modifier=Modifier.fillMaxWidth().height(220.dp))
        Heading(s(R.string.welcome),s(R.string.tagline))
        when(step) {
            0->{Text(s(R.string.language_notice)); Text(s(R.string.language_pending));TextButton(onClick={step++}){Text(s(R.string.continue_label))}}
            1->{Text(s(R.string.experience));listOf(R.string.beginner,R.string.experienced).forEachIndexed { i,id -> FilterChip(selected=experience==i,onClick={experience=i},label={Text(s(id))}) };FilterChip(selected=outdoors,onClick={outdoors=!outdoors},label={Text(s(R.string.outdoor))});TextButton(onClick={step++}){Text(s(R.string.continue_label))}}
            else->{Text(s(R.string.privacy_intro));Text(s(R.string.onboarding_optional));Button(onClick=done){Text(s(R.string.start_garden))}}
        }
        TextButton(onClick=done){Text(s(R.string.skip))}
    }
}
