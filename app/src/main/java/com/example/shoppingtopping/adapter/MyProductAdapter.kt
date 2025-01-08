package com.example.shoppingtopping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingtopping.R
import com.example.shoppingtopping.eventbus.UpdateCartEvent
import com.example.shoppingtopping.listener.ICartLoadListener
import com.example.shoppingtopping.listener.IRecyclerClickListener
import com.example.shoppingtopping.model.CartModel
import com.example.shoppingtopping.model.ProductModel
import com.example.shoppingtopping.product_list
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.view.View
import com.google.firebase.ktx.Firebase
import org.greenrobot.eventbus.EventBus

class MyProductAdapter(
    private val context:Context,
    private val list: List<ProductModel>,
            private val cartListener: ICartLoadListener
): RecyclerView.Adapter<MyProductAdapter.MyProductViewHolder>() {
    class MyProductViewHolder(itemView:android.view.View) : RecyclerView.ViewHolder(itemView),
        android.view.View.OnClickListener {
         var imageView: ImageView?=null
         var txtName:TextView?=null
         var txtPrice:TextView?=null
        private var clickListener:IRecyclerClickListener? = null
        fun setClickListener(clickListener: IRecyclerClickListener){
            this.clickListener = clickListener;
        }
        init{

            imageView=itemView.findViewById(R.id.imageView) as ImageView;
            txtName=itemView.findViewById(R.id.txtName) as TextView;
            txtPrice=itemView.findViewById(R.id.txtPrice) as TextView;
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: android.view.View?) {
            clickListener!!.onItemClickListener(v,adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyProductViewHolder {
        return MyProductViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_product_item,parent,false))

    }

    private fun MyProductViewHolder(itemView: LayoutInflater?) {

    }

    override fun onBindViewHolder(holder: MyProductViewHolder, position: Int) {
       Glide.with(context)
           .load(list[position].image)
           .into(holder.imageView!!)
        holder.txtName!!.text= StringBuilder().append(list[position].name)
        holder.txtPrice!!.text= StringBuilder("৳").append(list[position].price)
        holder.setClickListener(object:IRecyclerClickListener{
            override fun onItemClickListener(view: android.view.View?, position: Int) {
                addToCart(list[position])
            }

        })
    }

    private fun addToCart(productModel: ProductModel) {
        val userCart = FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID") // HERE IS SIMILAR USER ID, YOU CAN USE FIREBASE AUTH UID HERE
        userCart.child(productModel.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) // IF ITEM ALREADY IN CART, JUST UPDATE
                    {
                        val cartModel = snapshot.getValue(CartModel::class.java)
                        val updateData: MutableMap<String,Any> = HashMap()
                        cartModel!!.quantity = cartModel!!.quantity+1;
                        updateData["quantity"] =  cartModel!!.quantity
                        updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()
                        userCart.child(productModel.key!!)
                            .updateChildren(updateData)
                            .addOnSuccessListener {
                                EventBus.getDefault().postSticky(UpdateCartEvent())
                                cartListener.onLoadCartFailed("Success add to Cart")
                            }
                            .addOnFailureListener{e-> cartListener.onLoadCartFailed(e.message)}

                    }
                    else   // IF ITEM NOT IN CART, ADD NEW
                    {
                        val cartModel = CartModel()
                        cartModel.key = productModel.key
                        cartModel.name = productModel.name
                        cartModel.image = productModel.image
                        cartModel.price = productModel.price
                        cartModel.quantity = 1
                        cartModel.totalPrice = productModel.price!!.toFloat()
                        userCart.child(productModel.key!!)
                            .setValue(cartModel)
                            .addOnSuccessListener {
                                EventBus.getDefault().postSticky(UpdateCartEvent())
                                cartListener.onLoadCartFailed("Success add to Cart")
                            }
                            .addOnFailureListener{e-> cartListener.onLoadCartFailed(e.message)}

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cartListener.onLoadCartFailed(error.message)
                }
            })

    }

    override fun getItemCount(): Int {
        return list.size
    }

}