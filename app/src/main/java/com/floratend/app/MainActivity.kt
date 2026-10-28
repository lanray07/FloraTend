package com.floratend.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.speech.*
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResourceimport androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var canSpeak = false
    var listening by mutableStateOf(false)
    var transcript by mutableStateOf("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) canSpeak = (tts?.setLanguage(Locale.getDefault()) ?: -1) >= 0
        }
        setContent { FloraRoot(this) }
    }
    override fun onResume() { super.onResume(); (application as FloraApp).billing.refresh() }
    fun speak(text: String) { if (canSpeak) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "response") }
    fun listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { transcript = getString(R.string.voice_unavailable); return }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { speech ->
            speech.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { listening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { listening = false }
                override fun onError(error: Int) { listening = false; transcript = getString(R.string.voice_error) }
                override fun onResults(results: Bundle?) { listening = false; transcript = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty() }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            try { speech.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())); listening = true }
            catch (_: Exception) { transcript = getString(R.string.voice_unavailable); listening = false }
        }
    }
    override fun onDestroy() { recognizer?.destroy(); tts?.stop(); tts?.shutdown(); super.onDestroy() }    override fun onStop() { recognizer?.cancel(); listening=false; tts?.stop(); super.onStop() }
}

private val Forest = Color(0xFF315C45)
private val Ivory = Color(0xFFFAF8F2)
private val Clay = Color(0xFFC8795B)
@Composable fun s(id: Int) = stringResource(id)
@Composable fun Heading(title: String, subtitle: String? = null) {
    Text(title, fontSize = 34.sp, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.onBackground)
    if (subtitle != null) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
}
@Composable fun Field(value: String, id: Int, change: (String) -> Unit, lines: Int = 1) {
    OutlinedTextField(value, change, label = { Text(s(id)) }, modifier = Modifier.fillMaxWidth(), singleLine = lines == 1, minLines = lines)
}
@Composable fun FloraRoot(activity: MainActivity, vm: PlantViewModel = viewModel()) {
    val app = activity.application as FloraApp
    val dark by app.settings.dark.collectAsState(false)
    val onboarded by app.settings.onboarded.collectAsState(true)
    val scope = rememberCoroutineScope()
    val plants by vm.plants.collectAsState()
    val schedules by vm.schedules.collectAsState()
    val journal by vm.journal.collectAsState()
    val history by vm.history.collectAsState()
    val error by vm.error.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<Plant?>(null) }
    var selected by remember { mutableStateOf<Plant?>(null) }
    var delete by remember { mutableStateOf<Plant?>(null) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var descending by remember { mutableStateOf(false) }
    var newSchedule by remember { mutableStateOf(false) }
    var newJournal by remember { mutableStateOf(false) }
    var observation by remember { mutableStateOf<CareSchedule?>(null) }
    var journalEditor by remember { mutableStateOf<JournalEntry?>(null) }
    val colors = if (dark) darkColorScheme(primary = Color(0xFFADCFAF), secondary = Color(0xFFE8AD91), primaryContainer=Color(0xFF294632), secondaryContainer=Color(0xFF334737), surfaceVariant=Color(0xFF303B32)) else lightColorScheme(primary = Forest, secondary = Clay, background = Ivory, surface = Ivory, primaryContainer=Color(0xFFD9E7D7), onPrimaryContainer=Forest, secondaryContainer=Color(0xFFE1EBDF), onSecondaryContainer=Forest, surfaceVariant=Color(0xFFEDF0E7), onSurfaceVariant=Color(0xFF485448))
    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize()) {
            if (!onboarded) Onboarding { scope.launch { app.settings.set("onboarded", true) } }
            else BoxWithConstraints {
                val wide = maxWidth >= 720.dp
                val labels = listOf(R.string.garden,R.string.calendar,R.string.journal,R.string.voice,R.string.insights,R.string.settings)
                val icons = listOf(FloraIcons.Leaf,Icons.Outlined.DateRange,FloraIcons.Book,FloraIcons.Mic,FloraIcons.Chart,Icons.Outlined.Settings)
                Row(Modifier.fillMaxSize()) {
                    if (wide) NavigationRail(Modifier.fillMaxHeight().width(104.dp), containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f)) {
                        Icon(FloraIcons.Leaf, s(R.string.app_name), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 28.dp).size(36.dp))
                        labels.forEachIndexed { i, id -> NavigationRailItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icons[i],s(id)) }, label = { Text(s(id)) }) }
                    }
                    Scaffold(bottomBar = {
                        if (!wide) NavigationBar { labels.forEachIndexed { i,id -> NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icons[i], s(id)) }, label = { Text(s(id), fontSize = 10.sp) }) } }
                    }) { padding ->
                        Column(Modifier.padding(padding).padding(horizontal = if (wide) 32.dp else 20.dp).fillMaxSize()) {
                            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("FloraTend", fontFamily = FontFamily.Serif, fontSize = 23.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                Icon(Icons.Outlined.CheckCircle, s(R.string.local_records), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(s(R.string.local_records), fontSize = 12.sp)
                            }
                            when(tab) {
                                0 -> {
                                    Heading(s(R.string.your_garden),s(R.string.tagline))
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(pluralStringResource(R.plurals.collection_count,plants.size,plants.size), modifier = Modifier.weight(1f))
                                        Button(onClick = { editor = Plant(name = "") }) { Icon(Icons.Outlined.Add,null); Text(s(R.string.add_plant)) }
                                    }
                                    Field(query,R.string.search,{ query = it })
                                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                                        listOf("" to R.string.all,"Indoor" to R.string.indoor,"Outdoor" to R.string.outdoor,"Succulent" to R.string.succulents,"Cactus" to R.string.cacti,"Herb" to R.string.herbs,"Flowering" to R.string.flowering,"Tree" to R.string.trees,"Other" to R.string.other).forEach { (key,id) -> FilterChip(selected = category == key,onClick = { category = key },label = { Text(s(id)) },modifier = Modifier.padding(end=8.dp)) }
                                    }
                                    TextButton(onClick = { descending = !descending }) { Text(s(if(descending) R.string.sort_desc else R.string.sort_asc)) }
                                    val visible = plants.filter { (it.name.contains(query,true) || it.species.contains(query,true) || it.category.contains(query,true) || it.location.contains(query,true)) && (category.isEmpty() || category == it.category) }.let { if(descending) it.reversed() else it }
                                    if (plants.isEmpty()) Empty(s(R.string.empty_garden),s(R.string.empty_garden_body))
                                    else LazyVerticalGrid(columns=GridCells.Adaptive(340.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)) { items(visible,key={it.id}) { plant -> PlantCard(plant,schedules.filter { it.plantId == plant.id }) { selected = plant } } }
                                }
                                1 -> CareScreen(plants,schedules,vm,onAdd={newSchedule=true},onComplete={observation=it})
                                2 -> {
                                    Heading(s(R.string.growth_journal),s(R.string.journal_intro))
                                    Button(onClick={newJournal=true},enabled=plants.isNotEmpty()) { Text(s(R.string.add_entry)) }
                                    if(journal.isEmpty()) Empty(s(R.string.no_entries),s(R.string.no_entries_body))
                                    LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)) { items(journal,key={it.id}) { entry ->
                                        Card(Modifier.fillMaxWidth().clickable { journalEditor = entry }) { Column(Modifier.padding(16.dp)) {
                                            Text(plants.find { it.id == entry.plantId }?.name.orEmpty(), style=MaterialTheme.typography.titleMedium)
                                            Text(formatDate(entry.date),color=MaterialTheme.colorScheme.primary)
                                            Photo(entry.photo)
                                            Text(entry.note)
                                            val imperial by app.settings.imperial.collectAsState(false)
                                            entry.heightCm?.let { Text(s(if(imperial) R.string.height_in_value else R.string.height_value).format(java.text.NumberFormat.getNumberInstance().format(if(imperial) it / 2.54 else it))) }
                                            entry.leaves?.let { Text(s(R.string.leaves_value).format(it)) }
                                        } }
                                    }; journal.filter { it.photo != null }.groupBy { it.plantId }.filterValues { it.size >= 2 }.forEach { (plantId,photos) -> item { Text(s(R.string.comparison)+" · "+plants.find { it.id==plantId }?.name.orEmpty()); Row { listOf(photos.last(),photos.first()).forEach { entry -> Column(Modifier.weight(1f).padding(4.dp)) { Photo(entry.photo); Text(formatDate(entry.date)) } } } } } }
                                }
                                3 -> VoiceScreen(activity,plants,schedules,vm,onTab={tab=it},onAdd={editor=Plant(name="")},onSearch={query=it;category="";tab=0})
                                4 -> {
                                    Heading(s(R.string.insights),s(R.string.insights_intro))
                                    LazyColumn {
                                        item { Stat(s(R.string.total_plants),plants.size); Stat(s(R.string.completed_tasks),history.size); Stat(s(R.string.journal_entries),journal.size); Stat(s(R.string.upcoming),schedules.count { !it.paused && it.nextDate >= LocalDate.now().toString() }) }
                                        item { Text(s(R.string.collection_breakdown),style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(vertical=16.dp)); plants.groupBy { it.category }.forEach { (key,value) -> Stat(categoryLabel(key),value.size) } }
                                        item { Text(s(R.string.monthly_care),style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(vertical=16.dp)); history.groupBy { it.date.take(7) }.toSortedMap().forEach { (month,records) -> Stat(month,records.size) } }
                                        items(history.take(100)) { record -> ListItem(headlineContent={Text(plants.find { it.id == record.plantId }?.name.orEmpty())},supportingContent={Text("${careLabel(record.type)} · ${formatDate(record.date)} ${record.observation}")}) }
                                    }
                                }
                                else -> SettingsScreen(app,vm)
                            }
                        }
                    }
                }
            }
        }
        editor?.let { plant -> PlantEditor(plant,onDismiss={editor=null},onSave={vm.save(it) { editor=null }}) }
        selected?.let { plant -> AlertDialog(onDismissRequest={selected=null},title={Text(plant.name)},text={ Column(Modifier.verticalScroll(rememberScrollState())) { Photo(plant.photo); Text(plant.species); Text(categoryLabel(plant.category)); Text(plant.location); Text(s(R.string.acquired_value).format(formatDate(plant.acquired))); plant.potCm?.let { Text(s(R.string.pot_value).format(it)) }; Text(plant.soil); Text(plant.light); Text(plant.notes); Text(s(R.string.guidance),modifier=Modifier.padding(top=16.dp)); TextButton(onClick={vm.save(plant.copy(id=java.util.UUID.randomUUID().toString(),name=plant.name+" (2)")) { selected=null }}) { Text(s(R.string.duplicate)) }; TextButton(onClick={delete=plant;selected=null}) { Text(s(R.string.delete)) } } },confirmButton={TextButton(onClick={editor=plant;selected=null}) { Text(s(R.string.edit)) }},dismissButton={TextButton(onClick={selected=null}) { Text(s(R.string.close)) }}) }
        delete?.let { plant -> Confirm(s(R.string.delete_plant),{ delete=null }) { vm.delete(plant);delete=null } }
        if(newSchedule) ScheduleEditor(plants,{newSchedule=false}) { vm.schedule(it);newSchedule=false }
        if(newJournal) JournalEditor(plants,null,{newJournal=false},{vm.journal(it);newJournal=false},{})
        journalEditor?.let { entry -> JournalEditor(plants,entry,{journalEditor=null},{vm.journal(it);journalEditor=null},{vm.delete(entry);journalEditor=null}) }
        observation?.let { schedule -> AlertDialog(onDismissRequest={observation=null},title={Text(s(R.string.record_care))},text={Column { Text(s(R.string.check_first)); listOf(R.string.dry,R.string.moist,R.string.wet,R.string.watered,R.string.done).forEach { id -> val label=s(id); TextButton(onClick={vm.complete(schedule,label);observation=null}) { Text(label) } } }},confirmButton={TextButton(onClick={observation=null}){Text(s(R.string.cancel))}}) }
        error?.let { AlertDialog(onDismissRequest={vm.error.value=null},title={Text(s(R.string.attention))},text={Text(it)},confirmButton={TextButton(onClick={vm.error.value=null}){Text(s(R.string.close))}}) }
    }
}

@Composable fun categoryLabel(key: String): String = s(when(key) { "Indoor"->R.string.indoor;"Outdoor"->R.string.outdoor;"Succulent"->R.string.succulents;"Cactus"->R.string.cacti;"Herb"->R.string.herbs;"Flowering"->R.string.flowering;"Tree"->R.string.trees;else->R.string.other })
@Composable fun careLabel(key: String): String = s(when(key) {"soil"->R.string.soil_check;"water"->R.string.watering;"fertilise"->R.string.fertilising;"prune"->R.string.pruning;"repot"->R.string.repotting;"clean"->R.string.leaf_cleaning;"pest"->R.string.pest_check;else->R.string.custom_care})
fun formatDate(value:String):String = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) }.getOrDefault(value)
@Composable fun Photo(path:String?) { if(path != null) { if(File(path).exists()) AsyncImage(model=File(path),contentDescription=s(R.string.plant_photo),modifier=Modifier.fillMaxWidth().height(180.dp)); else Text(s(R.string.photo_missing)) } }
@Composable fun Empty(title:String,body:String) { Column(Modifier.fillMaxWidth().padding(vertical=16.dp),horizontalAlignment=Alignment.CenterHorizontally) { Image(painterResource(R.drawable.botanical_hero),null,Modifier.height(160.dp).fillMaxWidth()); Text(title,fontFamily=FontFamily.Serif,fontSize=24.sp,modifier=Modifier.padding(top=16.dp)); Text(body,modifier=Modifier.padding(top=8.dp)) } }
@Composable fun Stat(label:String,count:Int) { Row(Modifier.fillMaxWidth().padding(vertical=12.dp)) { Text(label,Modifier.weight(1f));Text(java.text.NumberFormat.getIntegerInstance().format(count),color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.titleLarge) }; HorizontalDivider() }
@Composable fun PlantCard(plant:Plant,schedules:List<CareSchedule>,click:()->Unit) {
    Card(onClick=click,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.45f))) {
        Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically) {
            if(plant.photo != null) AsyncImage(File(plant.photo),s(R.string.plant_photo),Modifier.size(88.dp)) else Icon(FloraIcons.Leaf,null,Modifier.size(52.dp),tint=MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start=20.dp).weight(1f)) { Text(plant.name,fontFamily=FontFamily.Serif,fontSize=25.sp); Text(plant.species, color=MaterialTheme.colorScheme.onSurfaceVariant); Text(plant.location,modifier=Modifier.padding(top=6.dp)); schedules.filter { !it.paused }.minByOrNull { it.nextDate }?.let { Text(s(R.string.next_check).format(formatDate(it.nextDate)),color=MaterialTheme.colorScheme.primary) } }
            Icon(Icons.Outlined.KeyboardArrowRight,s(R.string.open_plant))
        }
    }
}
@Composable fun Confirm(title:String,cancel:()->Unit,confirm:()->Unit) { AlertDialog(onDismissRequest=cancel,title={Text(title)},text={Text(s(R.string.delete_warning))},confirmButton={TextButton(onClick=confirm){Text(s(R.string.delete))}},dismissButton={TextButton(onClick=cancel){Text(s(R.string.cancel))}}) }
