package com.floratend.app

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.UUID

@Composable fun IdentificationPanel(photo:String?,photoBusy:Boolean,onSpecies:(String)->Unit) {
    val context=LocalContext.current
    val activity=LocalActivity.current
    val app=context.applicationContext as FloraApp
    val uri=LocalUriHandler.current
    val draftId=rememberSaveable { UUID.randomUUID().toString() }
    val model:IdentificationViewModel=viewModel(key="photo-identification-$draftId",factory=object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=IdentificationViewModel(app.plantIdentifier) as T
    })
    val state by model.state.collectAsState()
    var consent by rememberSaveable(photo) { mutableStateOf(false) }
    LaunchedEffect(photo) { model.bind(photo) }
    DisposableEffect(model,activity) { onDispose { if(activity?.isChangingConfigurations!=true)model.cancel() } }
    Column {
        TextButton(enabled=photo!=null && !photoBusy && !state.loading && model.available,onClick={consent=true}) { Text(s(R.string.identify_photo)) }
        if(!model.available)Text(s(R.string.identification_unavailable))
        if(state.loading) { CircularProgressIndicator();Text(s(R.string.identifying_photo));TextButton(onClick=model::cancel){Text(s(R.string.cancel))} }
        when(val result=state.result) {
            is IdentificationResult.Suggestions -> {
                Text(s(R.string.identification_suggestions))
                result.items.forEach { item ->
                    TextButton(onClick={model.selectedSpecies(photo,item.scientificName)?.let(onSpecies)}) { Column {
                        Text(item.scientificName)
                        if(item.commonNames.isNotEmpty())Text(item.commonNames.joinToString(", "))
                        Text(context.getString(R.string.identification_score,NumberFormat.getPercentInstance(context.resources.configuration.locales[0]).format(item.score)))
                    } }
                }
            }
            IdentificationResult.NoMatch -> Text(s(R.string.identification_no_match))
            IdentificationResult.DailyLimit -> Text(s(R.string.identification_limit))
            IdentificationResult.Unavailable -> Text(s(R.string.identification_error))
            null -> Unit
        }
        if(photo!=null)TextButton(onClick={uri.openUri("https://my.plantnet.org/terms_of_use")}) { Text(s(R.string.identification_terms)) }
    }
    if(consent)AlertDialog(onDismissRequest={consent=false},title={Text(s(R.string.identify_photo))},text={Text(s(R.string.identification_consent))},
        confirmButton={TextButton(onClick={consent=false;photo?.let { model.bind(it);model.send(it,context.resources.configuration.locales[0].language) }}){Text(s(R.string.send_photo))}},
        dismissButton={TextButton(onClick={consent=false}){Text(s(R.string.cancel))}})
}
