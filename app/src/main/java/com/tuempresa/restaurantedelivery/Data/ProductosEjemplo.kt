package com.tuempresa.restaurantedelivery.data

import com.tuempresa.restaurantedelivery.model.Producto

val productosEjemplo = listOf(
    Producto(
        id = 1,
        nombre = "Hamburguesa clásica",
        precio = 15.00,
        descripcion = "Pan artesanal, carne de res, queso, lechuga, tomate y salsa especial.",
        imagenUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=80"
    ),
    Producto(
        id = 2,
        nombre = "Pizza personal",
        precio = 20.00,
        descripcion = "Pizza personal con salsa de tomate, queso mozzarella y pepperoni.",
        imagenUrl = "https://images.unsplash.com/photo-1579751626657-72bc17010498?auto=format&fit=crop&w=900&q=80"
    ),
    Producto(
        id = 3,
        nombre = "Pollo broaster",
        precio = 18.00,
        descripcion = "Pollo crujiente acompañado de papas fritas y ensalada fresca.",
        imagenUrl = "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=900&q=80"
    ),
    Producto(
        id = 4,
        nombre = "Limonada",
        precio = 6.00,
        descripcion = "Limonada fría y refrescante, preparada al momento.",
        imagenUrl = "https://images.unsplash.com/photo-1523677011781-c91d1bbe2f5f?auto=format&fit=crop&w=900&q=80"
    )
)