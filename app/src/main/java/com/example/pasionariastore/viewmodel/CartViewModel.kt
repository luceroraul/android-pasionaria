package com.example.pasionariastore.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.pasionariastore.MyScreens
import com.example.pasionariastore.model.CartUIState
import com.example.pasionariastore.model.ProductCart
import com.example.pasionariastore.model.ProductCartWithData
import com.example.pasionariastore.model.ProductWithUnit
import com.example.pasionariastore.repository.CartRepository
import com.example.pasionariastore.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository, private val productRepository: ProductRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CartUIState())
    val state = _state

    private val _cartProducts = MutableStateFlow<List<ProductCartWithData>>(emptyList())
    val cartProducts = _cartProducts.asStateFlow()

    init {
//        checkDatabaseViewModel.checkData()
//        viewModelScope.launch(Dispatchers.IO) {
//            productRepository.getProductsWithUnitBySearch("pure").collect {
//                val products =
//                    if (it.isNullOrEmpty()) emptyList() else it
//            }
//
//            cartRepository.getProducts().collect {
//                _cartProducts.value =
//                    if (it.isNullOrEmpty())
//                        emptyList()
//                    else
//                        it
//            }
//        }
    }

    /**
     * Indica si es posible utilizar el buscador y restablece valores
     */

    fun initProductScreen(navController: NavHostController, canSearchProducts: Boolean) {
        navController.navigate(MyScreens.CartProduct.name)
        state.update {
            it.copy(
                canSearchProducts = canSearchProducts,
                currentSearch = "",
                showModalProductSearch = false,
                currentProductCart = null
            )
        }
    }

    /**
     * Actualiza el valor del buscador al momento de tipear
     */
    fun updateCurrentSearch(newValue: String) {
        state.update {
            it.copy(
                currentSearch = newValue
            )
        }
    }

    fun selectProductSearched(productSearched: ProductWithUnit) {
        state.update {
            it.copy(
                currentProductCart = ProductCartWithData(
                    productWithUnit = productSearched,
                    productCart = ProductCart(productId = productSearched.product.productId)
                ), showModalProductSearch = false
            )
        }
    }

    fun searchProducts(context: Context) {
        if (state.value.currentSearch.isNullOrEmpty()) {
            Toast.makeText(context, "Debe escribir algo para buscar", Toast.LENGTH_SHORT).show()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                productRepository.getProductsWithUnitBySearch(state.value.currentSearch)
                    .collect { products ->
                        if (products.isNullOrEmpty()) {
                            showMessage(context = context, message = "No hay coincidencias")
                        } else {
                            state.update {
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
        state.update {
            it.copy(showModalProductSearch = false)
        }
    }

    fun updateCurrentQuantity(newQuantity: String) {
        state.update {
            it.copy(
                currentProductCart = it.currentProductCart!!.copy(
                    it.currentProductCart.productCart.copy(
                        quantity = newQuantity
                    )
                )
            )
        }
    }

    fun calculateCartPrice(): String =
        (state.value.productCartList.map { p -> p.productCart.totalPrice }
            .reduceOrNull { acc, price -> acc + price } ?: 0.0).toString()

    fun calculatePrice(): String {
        var result = 0.0
        var quantity = 0.0
        var price = 0.0
        state.value.currentProductCart?.let { productCart ->
            quantity = productCart.productCart.quantity.toDoubleOrNull() ?: 0.0
            price = productCart.productWithUnit.product.priceList
            result = (price * quantity / 1000)
            state.update {
                it.copy(
                    currentProductCart = productCart.copy(
                        productCart = productCart.productCart.copy(
                            totalPrice = result
                        )
                    )
                )
            }
        }
        return "ARS $result"
    }

    fun canAddProductToCart(): Boolean {
        var result = false
        if (state.value.currentProductCart != null) result =
            (state.value.currentProductCart!!.productCart.quantity.toDoubleOrNull() ?: 0.0) > 0.0
        return result
    }

    fun addProductToCart(navController: NavHostController, context: Context) {
        // persisto el producto en la base de datos
        viewModelScope.launch(Dispatchers.IO) {
            state.value.currentProductCart?.productCart?.let {
                var message = "El producto fue agregado al pedido"
                cartRepository.insertProductCart(it)
                if (it.productCartId != 0L) message = "El producto fue actualizado"
                showMessage(context = context, message = message)
            }
        }
        navController.navigate(MyScreens.Cart.name)
    }

    fun removeProductFromCart(product: ProductCartWithData, context: Context) {
        state.value.productCartList.remove(product)
        Toast.makeText(context, "El producto fue removido del pedido", Toast.LENGTH_SHORT).show()
    }

    fun updateProductCart(
        product: ProductCartWithData, navController: NavController, context: Context
    ) {
        state.update {
            it.copy(
                currentProductCart = product
            )
        }
        navController.navigate(MyScreens.CartProduct.name)
    }

    fun showMessage(message: String, context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

}