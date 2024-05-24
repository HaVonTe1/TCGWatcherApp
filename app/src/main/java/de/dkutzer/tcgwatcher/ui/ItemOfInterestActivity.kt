package de.dkutzer.tcgwatcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.cards.entity.BaseProductModel

@Composable
fun ItemOfInterestActivity(ioiList: List<BaseProductModel>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(ioiList.size) {
            ItemOfInterestCard(
                productModel = ioiList[it],
                iconRowContent =  {ItemViewCardIconRow()},
                showLastUpdated= true,
                modifier = modifier)
        }
    }
}

@Composable
fun ItemViewCardIconRow( modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(1.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        ClickableIconButton(icon = Icons.TwoTone.Edit, desc = stringResource(id = R.string.editDesc), onClick = {})
        ClickableIconButton(icon = Icons.TwoTone.Delete, desc = stringResource(id = R.string.deleteDesc), onClick = {})
    }
}






//@Preview(showBackground = true)
//@Composable
//fun TestItemPreview() {
//    ItemOfInterestActivity(Datasource().loadMockData())
//
//}