package com.example.pasionariastore.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pasionariastore.data.Datasource
import com.example.pasionariastore.model.Cart
import com.example.pasionariastore.model.format
import com.example.pasionariastore.model.state.CartListUIState
import com.example.pasionariastore.model.state.CartStatus
import com.example.pasionariastore.ui.preview.CartRepositoryFake
import com.example.pasionariastore.ui.theme.PasionariaStoreTheme
import com.example.pasionariastore.viewmodel.CartListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
@Preview
fun CartFormPreview(modifier: Modifier = Modifier) {
    val state = CartListUIState()
    PasionariaStoreTheme(darkTheme = true) {
        CartForm(
            modifier = modifier,
            stateButtons = state.stateFilters,
            onUpdateChip = {})

    }
}

@Composable
@Preview
fun CartItemPreview(modifier: Modifier = Modifier) {
    PasionariaStoreTheme(darkTheme = true) {
        Column {
            CartItem(modifier = modifier, cart = Datasource.carts.get(0), onDeleteCartClicked = {})
            CartItem(modifier = modifier, cart = Datasource.carts.get(1), onDeleteCartClicked = {})
            CartItem(modifier = modifier, cart = Datasource.carts.get(2), onDeleteCartClicked = {})
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ScreenPreivew(modifier: Modifier = Modifier) {
    PasionariaStoreTheme(darkTheme = true) {
        CartListScreen(
            modifier = modifier,
            cartListViewModel = CartListViewModel(cartRepository = CartRepositoryFake()),
            stateFlow = MutableStateFlow(CartListUIState(carts = Datasource.carts.toMutableStateList())),
            onCreateNewCartClicked = {},
            onDeleteCartClicked = {}
        )
    }
}


@Composable
fun CartListScreen(
    modifier: Modifier = Modifier,
    cartListViewModel: CartListViewModel,
    stateFlow: StateFlow<CartListUIState>,
    onCreateNewCartClicked: () -> Unit,
    onDeleteCartClicked: (Cart) -> Unit
) {
    val state by stateFlow.collectAsState()
    Box(modifier = modifier) {
        Column(modifier = modifier.fillMaxSize()) {
            CartForm(
                modifier = modifier,
                stateButtons = state.stateFilters,
                onUpdateChip = { cartListViewModel.updateChipStatus(it) }
            )
            CartList(modifier, state.carts, onDeleteCartClicked)
        }
        FloatingActionButton(
            onClick = onCreateNewCartClicked,
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(25.dp)
        ) {
            Icon(Icons.Filled.Add, "New Cart")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartForm(
    modifier: Modifier,
    stateButtons: MutableList<CartStatus>,
    onUpdateChip: (CartStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CartFormFilterStates(
                modifier = modifier,
                status = stateButtons,
                onUpdateChip = { onUpdateChip(it) })
        }
    }
}

@Composable
fun CartFormFilterStates(
    modifier: Modifier,
    status: MutableList<CartStatus>,
    onUpdateChip: (CartStatus) -> Unit
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
        status.forEach {
            FilterChip(
                modifier = modifier
                    .height(IntrinsicSize.Max)
                    .weight(1f),
                selected = it.enabled,
                onClick = { onUpdateChip(it) },
                label = {
                    Text(
                        text = it.label,
                        modifier = modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                shape = CutCornerShape(0.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorResource(id = it.backgroundColorActive),
                    selectedLabelColor = Color.White,
                    labelColor = Color.White,
                    containerColor = colorResource(id = it.backgroundColorInactive)
                ),
            )
        }
    }
}

@Composable
fun CartList(modifier: Modifier, carts: MutableList<Cart>, onDeleteCartClicked: (Cart) -> Unit) {
    LazyColumn {
        items(carts) {
            CartItem(modifier, it, onDeleteCartClicked = onDeleteCartClicked)
        }
    }
}

@Composable
fun CartItem(modifier: Modifier, cart: Cart, onDeleteCartClicked: (Cart) -> Unit) {
    Card(
        modifier = modifier
            .padding(5.dp)
            .alpha(0.9f),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.secondary)
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
            CartItemStatusLabel(modifier = modifier, cart = CartStatus.valueOf(cart.status))
            Column(modifier = modifier.padding(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text(text = "#${cart.id}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = cart.totalPrice.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cart.dateCreated.format(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(text = cart.usernameSeller, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (cart.status.equals(CartStatus.PENDING.name))
                CardActionButtons(
                    onCartProductClicked = { /*TODO*/ },
                    onDeleteProductClicked = { onDeleteCartClicked(cart) },
                    labelDelete = "Eliminar",
                    labelEdit = "Editar productos"
                )
        }
    }
}

@Composable
fun CartItemStatusLabel(modifier: Modifier, cart: CartStatus) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(colorResource(id = cart.backgroundColorActive))
    ) {
        Text(
            text = cart.label,
            textAlign = TextAlign.Center,
            modifier = modifier,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }

}


