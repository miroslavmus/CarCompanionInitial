package uni.fmi.miroslav.carcompanion.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.models.ModelItem
import java.util.ArrayList

class ItemRecyclerAdapter (var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val ERROR_TAG: String = "ItemRecyclerAdapterError"

    private var items: List<ModelItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_list_item,
                parent,
                false
            ), onItemClickListener
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

    fun submitList(itemList: List<ModelItem>){
        items = itemList
    }

    class ItemViewHolder constructor( itemView: View, var onItemClickListener: OnItemClickListener): RecyclerView.ViewHolder(itemView){

        //define views for item to be shown
        val itemLayout: ConstraintLayout = itemView.findViewById(R.id.itemLayout)
        val picture : ImageView = itemView.findViewById(R.id.item_image)
        val name : TextView = itemView.findViewById(R.id.item_name)
        val message : TextView = itemView.findViewById(R.id.item_message)
        val value1 : TextView = itemView.findViewById(R.id.item_value1)
        val value2 : TextView = itemView.findViewById(R.id.item_value2)
        val field1 : TextView = itemView.findViewById(R.id.item_field1)
        val field2 : TextView = itemView.findViewById(R.id.item_field2)

        fun bind(item: ModelItem){
            picture.setImageResource(picture.resources.getIdentifier(item.picturePath, "drawable", (onItemClickListener as Activity).packageName))
            name.text = item.name
            message.text = item.message
            value1.text = item.valueField1Text
            value2.text = item.valueField2Text
            field1.text = item.changeField1Text
            field2.text = item.changeField2Text

            itemLayout.setOnClickListener { onItemClickListener.onItemClick(item, it) }
        }
    }
    interface OnItemClickListener{
        fun onItemClick(modelItem: ModelItem, view: View)
    }

}