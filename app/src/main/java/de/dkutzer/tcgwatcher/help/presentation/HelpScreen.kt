package de.dkutzer.tcgwatcher.help.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

private const val SOURCE_URI = "https://github.com/HaVonTe1/TCGWatcherApp"
private const val LICENCE_URI = "https://opensource.org/license/mit"

@Composable
fun HelpScreen() {
    val context = LocalContext.current
    Column {
        Row (modifier = Modifier.padding(8.dp)) {
            Text(text = "Source: ", modifier = Modifier.padding(8.dp))
            ClickAbleTextForUri("github", SOURCE_URI, context)
        }
        Row (modifier = Modifier.padding(8.dp)) {
            Text(text = "Licence: ", modifier = Modifier.padding(8.dp))
            ClickAbleTextForUri("MIT License",  LICENCE_URI, context)
        }

            Text(text = "Used Libraries and Frameworks: ", modifier = Modifier.padding(8.dp))
            LibrariesContainer(
                Modifier.fillMaxSize()
            )


    }
}

@Composable
private fun ClickAbleTextForUri(
    text: String,
    uri: String,
    context: Context
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            },
        color = Color.Blue,
        textDecoration = TextDecoration.Underline
    )
}

@PreviewLightDark
@Composable
fun HelpScreenPreview(modifier: Modifier = Modifier) {
    TCGWatcherTheme {
        HelpScreen()
    }
}

