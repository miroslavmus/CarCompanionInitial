package uni.fmi.miroslav.carcompanion.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelItem


class ItemFragment : Fragment(),
    OnActivityAutoFill {

    private lateinit var img: ImageView
    private lateinit var name: TextView
    private lateinit var value1: TextView
    private lateinit var value2: TextView
    private lateinit var field1: TextView
    private lateinit var field2: TextView
    private lateinit var message: TextView

    private lateinit var listener: FragmentForm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.layout_list_item, container, false)

        img = v.findViewById(R.id.item_image)
        name = v.findViewById(R.id.item_name)
        value1 = v.findViewById(R.id.item_value1)
        value2 = v.findViewById(R.id.item_value2)
        field1 = v.findViewById(R.id.item_field1)
        field2 = v.findViewById(R.id.item_field2)
        message = v.findViewById(R.id.item_message)


        return v
    }

    //attach our activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as FragmentForm
        listener.onFragmentAttach(this)
    }

    override fun onAutoFill(modelItem: ModelItem) {
        img.setImageResource(img.resources.getIdentifier(modelItem.picturePath, "drawable", (listener as Activity).packageName))
        name.text = modelItem.name
        value1.text = modelItem.valueField1Text
        value2.text = modelItem.valueField2Text
        field1.text = modelItem.changeField1Text
        field2.text = modelItem.changeField2Text
        message.text = modelItem.message
    }
}