package uni.fmi.miroslav.carcompanion.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.models.ModelImage
import java.util.ArrayList

class ImageRecyclerAdapter (var onImageClickListener: OnImageClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val ERROR_TAG: String = "ImageRecyclerAdapterError"

    private var items: List<ModelImage> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_list_image,
                parent,
                false
            ), onImageClickListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {

            is ItemViewHolder -> {
                holder.bind(items[position])
            }

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(itemList: List<ModelImage>){
        items = itemList
    }

    class ItemViewHolder(itemView: View, var onImageClickListener: OnImageClickListener): RecyclerView.ViewHolder(itemView){

        private val picture : ImageView = itemView.findViewById(R.id.imageItem)


        fun bind(item: ModelImage){
            picture.setImageResource(picture.resources.getIdentifier(item.image, "drawable", (onImageClickListener as Activity).packageName))

            picture.setOnClickListener{
                onImageClickListener.onImageClick(item.image)
            }
        }
    }
    
    interface OnImageClickListener {
        fun onImageClick(image: String)
    }
}