package com.example.shoppingtopping.listener

import com.example.shoppingtopping.model.ProductModel

interface IProductLoadListener {
    fun onProductLoadSuccess(productModelList:List<ProductModel>?)
    fun onProductLoadFailed(message:String?)
}