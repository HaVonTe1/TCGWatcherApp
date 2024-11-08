package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme

@Composable
fun IconWithText(
    icon: Painter,
    desc: String,
    text: String,
    testStyle: TextStyle,
    iconHeigh: Int = 48,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(4.dp)
                .width(iconHeigh.dp)
                .height(iconHeigh.dp),
            painter = icon,
            contentDescription = desc
        )
        Spacer(Modifier.width(32.dp))

        Text(
            text = text,
            style = testStyle
        )

    }
}

@Composable
@PreviewLightDark
fun IconWithTextPreview() {

    TCGWatcherTheme {
        IconWithText(
            icon = painterResource(id = R.drawable.ic_launcher_foreground),
            desc = "test",
            text = "test",
            testStyle = MaterialTheme.typography.bodyMedium
        )

    }
}