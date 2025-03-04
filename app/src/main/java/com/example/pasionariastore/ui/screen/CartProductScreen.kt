package com.example.pasionariastore.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pasionariastore.R
import com.example.pasionariastore.data.Datasource
import com.example.pasionariastore.model.state.CartUIState
import com.example.pasionariastore.model.ProductCartWithData
import com.example.pasionariastore.model.ProductWithUnit
import com.example.pasionariastore.ui.theme.PasionariaStoreTheme
import kotlinx.coroutines.flow.collectLatest


@Preview
@Composable
fun ProductScreenPreview() {
    PasionariaStoreTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CartProductScreen(
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                onAddButtonClicked = {},
                onCancelButtonClicked = {},
                priceCalculated = "123",
                onSearchProducts = {},
                updateCurrentSearch = {},
                onCancelSearch = {},
                onProductSearchClicked = {},
                formatPriceNumber = { "203" },
                state = CartUIState(),
                updateQuantity = {},
            )
        }
    }
}

@Composable
fun CartProductScreen(
    modifier: Modifier = Modifier,
    onAddButtonClicked: () -> Unit,
    onCancelButtonClicked: () -> Unit,
    state: CartUIState,
    onProductSearchClicked: (ProductWithUnit) -> Unit,
    onCancelSearch: () -> Unit,
    formatPriceNumber: (Double) -> String,
    priceCalculated: String,
    onSearchProducts: () -> Unit,
    updateCurrentSearch: (String) -> Unit,
    updateQuantity: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        state.lastSearch.collectLatest {
            focusRequester.requestFocus()
            focusManager.moveFocus(FocusDirection.Down)
        }
    }
    if (state.showModalProductSearch) {
        // Antes de abrir el modal tengo que ver si existen coincidencias
        ModalSearchProduct(
            productList = state.currentProductSearcheds,
            onProductSearchClicked = { onProductSearchClicked(it) },
            search = state.currentSearch,
            modifier = modifier,
            onCancelSearch = onCancelSearch
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (state.canSearchProducts) {
            Card {
                ProductSearcher(
                    modifier.fillMaxWidth(),
                    canSearchProducts = state.canSearchProducts,
                    onSearchProducts = onSearchProducts,
                    currentSearch = state.currentSearch,
                    updateCurrentSearch = updateCurrentSearch,
                    focusRequester = focusRequester
                )
            }
            Spacer(modifier = modifier.padding(10.dp))
        }
        Card(modifier = modifier.weight(1f)) {
            ProductDescription(
                modifier.fillMaxWidth(),
                state.currentProductCart,
                formatValue = formatPriceNumber
            )
        }
        Spacer(modifier = modifier.padding(10.dp))
        Card {
            ProductFormCalculator(
                modifier = modifier,
                state = state,
                updateQuantity = { updateQuantity(it) },
                priceCalculated = priceCalculated,
                focusRequester = focusRequester,
                onAddButtonClicked = onAddButtonClicked
            )
            CartProductActionButtons(
                modifier = modifier,
                onCancelButtonClicked = onCancelButtonClicked,
                onAddButtonClicked = onAddButtonClicked,
                productCartWithData = state.currentProductCart,
            )
        }
    }

}

@Composable
fun ProductDescription(
    modifier: Modifier = Modifier,
    relation: ProductCartWithData?,
    formatValue: (Double) -> String
) {
    var name: String = "Nombre del producto"
    var description: String = "Descripcion del producto"
    var price: String = "0.0"
    var unit: String = "SIN UNIDAD"
    relation?.productWithUnit?.let {
        it.product.let { product ->
            name = product.name
            description = product.description
            price = formatValue(product.priceList * it.unit.value)
        }
        unit = it.unit.nameType
    }
    Card(modifier = modifier) {
        Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
            DescriptionItem(
                title = "Producto",
                description = name,
                modifier = modifier
            )
            DescriptionItem(
                title = "Descripcion", description = description, modifier = modifier
            )
            DescriptionItem(
                title = "Precio Lista",
                description = formatValue(relation?.productWithUnit?.product?.priceList ?: 0.0),
                modifier = modifier
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier.weight(1f)
                ) {
                    DescriptionItem(
                        title = "Precio",
                        description = price,
                        modifier = modifier
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier.weight(1f)
                ) {
                    DescriptionItem(title = "Unidad", description = unit, modifier = modifier)
                }
            }
        }

    }
}

@Composable
fun DescriptionItem(title: String, description: String, modifier: Modifier) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth()
        )
        Text(
            text = description,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth()
        )

    }
}

@Composable
fun ProductSearcher(
    modifier: Modifier,
    updateCurrentSearch: (String) -> Unit,
    onSearchProducts: () -> Unit,
    canSearchProducts: Boolean,
    currentSearch: String,
    focusRequester: FocusRequester
) {
    TextField(
        enabled = canSearchProducts,
        value = currentSearch,
        onValueChange = { updateCurrentSearch(it) },
        singleLine = true,
        label = { Text(text = "Buscador de productos") },
        keyboardActions = KeyboardActions(onSearch = { onSearchProducts() }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        leadingIcon = {
            Icon(painter = painterResource(id = R.drawable.search), contentDescription = "search")
        },

        modifier = modifier.focusRequester(focusRequester)
    )
}

@Composable
fun CartProductActionButtons(
    modifier: Modifier,
    onCancelButtonClicked: () -> Unit,
    onAddButtonClicked: () -> Unit,
    productCartWithData: ProductCartWithData?,
) {
    Row {
        Button(
            onClick = onCancelButtonClicked,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.delete_active)),
            shape = RoundedCornerShape(
                0.dp
            ),
            modifier = modifier
                .weight(1f)
                .height(IntrinsicSize.Max),
        ) {
            Text(text = "Cancelar")
        }
        Button(
            enabled = (productCartWithData?.productCart?.quantity?.toDoubleOrNull() ?: 0.0) > 0.0,
            onClick = onAddButtonClicked,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.update_active)),
            shape = RoundedCornerShape(
                0.dp
            ),
            modifier = modifier
                .weight(1f)
                .height(IntrinsicSize.Max),
        ) {
            Text(text = if (productCartWithData?.productCart != null) "Agregar" else "Actualizar")
        }
    }
}

@Composable
fun ProductFormCalculator(
    state: CartUIState,
    modifier: Modifier,
    priceCalculated: String,
    updateQuantity: (String) -> Unit,
    focusRequester: FocusRequester,
    onAddButtonClicked: () -> Unit
) {
    Card(modifier = modifier.padding(10.dp)) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            DescriptionItem(
                title = "Calculadora",
                description = priceCalculated,
                modifier = modifier
            )
            TextField(
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onAddButtonClicked() }),
                value = state.currentProductCart?.productCart?.quantity ?: "",
                onValueChange = { updateQuantity(it) },
                modifier = modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                enabled = state.currentProductCart != null,
                label = { Text(text = "Cantidad del producto") },
            )
        }
    }
}

@Preview
@Composable
fun ModalSearchProductPreview(modifier: Modifier = Modifier) {
    ModalSearchProduct(
        productList = Datasource.productsWithUnit,
        "prueba",
        onProductSearchClicked = { },
        onCancelSearch = {})
}

@Composable
fun ModalSearchProduct(
    productList: List<ProductWithUnit>,
    search: String,
    onProductSearchClicked: (ProductWithUnit) -> Unit,
    onCancelSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onCancelSearch) {
        Card {
            Column {
                Text(text = "Buscando ... $search ...")
                LazyColumn(modifier = modifier.height(400.dp)) {
                    items(productList) {
                        ModalSearchProductItem(it, onProductSearchClicked, modifier)
                    }
                }
                Button(
                    onClick = onCancelSearch, modifier = modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.delete_active))
                ) {
                    Text(text = "Cancelar")
                }
            }
        }
    }
}

@Composable
fun ModalSearchProductItem(
    productWithUnit: ProductWithUnit,
    onProductSearchClicked: (ProductWithUnit) -> Unit,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .clickable { onProductSearchClicked(productWithUnit) }
    ) {
        Text(
            text = productWithUnit.product.name,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = modifier.fillMaxWidth()
        )
        Text(
            text = productWithUnit.product.description,
            fontStyle = FontStyle.Italic,
            fontSize = 12.sp,
            modifier = modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = modifier.padding(5.dp))
    }
}

