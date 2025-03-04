package com.example.pasionariastore.data

import com.example.pasionariastore.model.Cart
import com.example.pasionariastore.model.Product
import com.example.pasionariastore.model.ProductCart
import com.example.pasionariastore.model.ProductCartWithData
import com.example.pasionariastore.model.ProductWithUnit
import com.example.pasionariastore.model.Unit
import java.util.Date

object Datasource {
    val units: List<Unit> = listOf(
        Unit(unitId = 1, name = "x50gr", value = 0.05),
        Unit(unitId = 2, name = "x100gr", value = 0.1),
        Unit(unitId = 3, name = "x250gr", value = 0.25),
        Unit(unitId = 4, name = "x500gr", value = 0.5),
        Unit(unitId = 5, name = "x750gr", value = 0.75),
        Unit(unitId = 6, name = "x1kg", value = 1.0),
        Unit(unitId = 7, name = "Unidad", value = 1.0, nameType = "Unidad"),
    )

    val products: List<Product> = listOf(
        Product(
            name = "Aceitunas", description = "Libanti x 500g", priceList = 1595.0, unitId = 1
        ), Product(
            name = "Pera", description = "Libanti x 500g", priceList = 23.0, unitId = 2
        ), Product(
            name = "Manzana", description = "Libanti x 500g", priceList = 344.0, unitId = 3
        ), Product(
            name = "Carne", description = "Libanti x 500g", priceList = 500.0, unitId = 4
        ), Product(
            name = "Especie", description = "Libanti x 500g", priceList = 24500.0, unitId = 5
        )
    )

    val productsWithUnit: List<ProductWithUnit> = listOf(
        ProductWithUnit(
            product = products.get(0), unit = units.get(0)
        ), ProductWithUnit(
            product = products.get(1), unit = units.get(1)
        ), ProductWithUnit(
            product = products.get(2), unit = units.get(2)
        ), ProductWithUnit(
            product = products.get(3), unit = units.get(3)
        ), ProductWithUnit(
            product = products.get(4), unit = units.get(4)
        )
    )

    val cartProducts: List<ProductCartWithData> = listOf(
        ProductCartWithData(
            productCart = ProductCart(
                quantity = "233", productCartId = 2, totalPrice = 2500.0, productId = 3
            ), productWithUnit = productsWithUnit.get(2)
        ), ProductCartWithData(
            productCart = ProductCart(
                quantity = "500", productCartId = 3, totalPrice = 3625.0, productId = 3
            ), productWithUnit = productsWithUnit.get(3)
        )
    )

    val carts: List<Cart> = listOf(
        Cart(status = "INACTIVE"),
        Cart(status = "PENDING"),
        Cart(status = "FINALIZED"),
        Cart(),
        Cart(dateCreated = Date(15624)),
        Cart(status = "FINALIZED", dateCreated = Date(1562624)),
        Cart(status = "PENDING", dateCreated = Date(155959624)),
        Cart(status = "INACTIVE", dateCreated = Date(124)),

        )
}