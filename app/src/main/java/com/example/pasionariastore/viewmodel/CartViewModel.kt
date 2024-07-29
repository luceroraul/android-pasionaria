package com.example.pasionariastore.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.pasionariastore.MyScreens
import com.example.pasionariastore.model.ProductCart
import com.example.pasionariastore.model.ProductCartWithData
import com.example.pasionariastore.model.ProductWithUnit
import com.example.pasionariastore.model.state.CartUIState
import com.example.pasionariastore.repository.CartRepository
import com.example.pasionariastore.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val checkDatabaseViewModel: CheckDatabaseViewModel
) : ViewModel() {
    private var _state = MutableStateFlow(CartUIState())
    val state = _state.asStateFlow()

    fun cleanState() {
        _state.update {
            CartUIState()
        }
    }

    fun initScreenByCart(cartId: Long) {
        cleanState()
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.getCartWithData(cartId).collect { cart ->
                if (cart != null) _state.update {
                    it.copy(cartWithData = mutableStateOf(cart))
                }
            }
        }
    }

    /**
     * Indica si es posible utilizar el buscador y restablece valores
     */

    fun goToAddNewProductCartScreen(navController: NavHostController) {
        navController.navigate(MyScreens.CartProduct.route)
        _state.update { CartUIState() }
        viewModelScope.launch {
            delay(1000)
            state.value.lastSearch.emit(value = Unit)
        }
    }

    /**
     * Actualiza el valor del buscador al momento de tipear
     */
    fun updateCurrentSearch(newValue: String) {
        _state.update {
            it.copy(
                currentSearch = newValue
            )
        }
    }

    fun selectProductSearched(productSearched: ProductWithUnit) {
        // TODO arreglar el cartId de los productCart ya que tuve que colocarlo en 0 para que no moleste
        _state.update {
            it.copy(
                currentProductCart = ProductCartWithData(
                    productWithUnit = productSearched, productCart = ProductCart(
                        productId = productSearched.product.productId,
                        cartId = it.cartWithData.value.cart.id
                    )
                ), showModalProductSearch = false
            )
        }
        // notifico nuevo envio
        viewModelScope.launch {
            delay(1000)
            state.value.lastSearch.emit(value = Unit)
            state.value.lastSearch.emit(value = Unit)
        }
    }

    fun searchProducts(context: Context) {
        if (state.value.currentSearch.isEmpty()) {
            Toast.makeText(context, "Debe escribir algo para buscar", Toast.LENGTH_SHORT).show()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                productRepository.getProductsWithUnitBySearch(state.value.currentSearch)
                    .collect { products ->
                        if (products.isEmpty()) {
                            showMessage(context = context, message = "No hay coincidencias")
                        } else {
                            _state.update {
                                it.copy(
                                    currentProductSearcheds = products,
                                    showModalProductSearch = true
                                )
                            }
                        }
                    }
            }
        }
    }

    fun cancelProductSearch() {
        _state.update {
            it.copy(showModalProductSearch = false)
        }
    }

    fun updateCurrentQuantity(newQuantity: String) {
        _state.update {
            it.copy(
                currentProductCart = it.currentProductCart.copy(
                    it.currentProductCart.productCart.copy(
                        quantity = newQuantity
                    )
                )
            )
        }
    }

    fun calculateCartPrice(): String {
        val products = state.value.cartWithData.value.productCartWithData ?: emptyList()
        var result = 0.0
        if (products.isNotEmpty()) {
            result = (products.map { p -> p.productCart.totalPrice }
                .reduce { acc, price -> acc + price })
        }
        return formatPriceNumber(result)
    }


    fun calculatePrice(): String {
        var result = 0.0
        var quantity = 0.0
        var price = 0.0
        state.value.currentProductCart.let { productCart ->
            quantity = productCart.productCart.quantity.toDoubleOrNull() ?: 0.0
            price = productCart.productWithUnit.product.priceList
            result = (price * quantity / 1000)
            _state.update {
                it.copy(
                    currentProductCart = productCart.copy(
                        productCart = productCart.productCart.copy(
                            totalPrice = result
                        )
                    )
                )
            }
        }
        return formatPriceNumber(result)
    }

    fun addProductToCart(context: Context) {
        // persisto el producto en la base de datos
        viewModelScope.launch(Dispatchers.IO) {
            state.value.let {
                it.currentProductCart
            }

            state.value.currentProductCart.productCart.let {
                var message = "El producto fue agregado al pedido"
                cartRepository.insertProductCart(it)
                if (it.productCartId != 0L) message = "El producto fue actualizado"
                showMessage(context = context, message = message)
            }
        }
    }

    fun removeProductFromCart(data: ProductCartWithData, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.deleteProductCart(productCart = data.productCart)
            showMessage(context = context, message = "El producto fue removido del pedido")

        }
    }

    fun goToUpdateProductCart(
        product: ProductCartWithData, navController: NavController
    ) {
        _state.update {
            it.copy(
                currentProductCart = product,
                canSearchProducts = false,
            )
        }
        navController.navigate(MyScreens.CartProduct.route)
        viewModelScope.launch {
            delay(1000)
            state.value.lastSearch.emit(Unit)
        }
    }

    fun showMessage(message: String, context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun formatPriceNumber(value: Double): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.setMaximumFractionDigits(2)
        format.currency = Currency.getInstance("ARS")

        return format.format(value)
    }

    fun canEditQuantity(): Boolean{
        return state.value.currentProductCart.productCart.cartId != 0L
    }

}