package com.example.shoppingtopping.listener

import com.example.shoppingtopping.model.CartModel

interface ICartLoadListener {
    fun onLoadCartSuccess(cartModelList:List<CartModel>)
    fun onLoadCartFailed(message:String?)
}