package uni.fmi.miroslav.carcompanion.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.models.ModelFix
import java.util.ArrayList

class FixRecyclerAdapter (var onFixClickListener: OnFixClickListener, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val ERROR_TAG: String = "FixRecyclerAdapterError"

    private var items: List<ModelFix> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_fix_item,
                parent,
                false
            ),
            onFixClickListener,
            context
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

    fun submitList(itemList: List<ModelFix>){
        items = itemList
    }

    class ItemViewHolder constructor(itemView: View, var onFixClickListener: OnFixClickListener, val context: Context ): RecyclerView.ViewHolder(itemView){

        //define views for item to be shown
        val itemLayout: LinearLayout = itemView.findViewById(R.id.fixItemLayout)
        val fixString: TextView = itemView.findViewById(R.id.stringFixTextView)

        fun bind(item: ModelFix){
            val string =  "${context.getString(R.string.km)} : ${item.km}; ${context.getString(
                R.string.date
            )} : ${item.date};"
            fixString.text = string
            itemLayout.setOnClickListener { onFixClickListener.onFixClick(item, it) }
        }
    }
    interface OnFixClickListener{
        fun onFixClick(modelItem: ModelFix, view: View)
    }

}