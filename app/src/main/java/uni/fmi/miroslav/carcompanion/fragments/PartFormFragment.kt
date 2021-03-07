package uni.fmi.miroslav.carcompanion.fragments

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import uni.fmi.miroslav.carcompanion.tools.Calc
import uni.fmi.miroslav.carcompanion.tools.Database
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelItem


class PartFormFragment : Fragment(),
    OnActivityAutoFill {


    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as FragmentForm
        parentActivity.onFragmentAttach(this)
    }

    private lateinit var parentActivity: FragmentForm

    private lateinit var distanceET : EditText
    private lateinit var timeET : EditText
    private lateinit var nameET : EditText
    private lateinit var submitBtn : Button
    private lateinit var kmCheckBox: CheckBox
    private lateinit var timeCheckBox: CheckBox
    private var isMetric = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_part_form, container, false)

        //declare our fields
        distanceET = v.findViewById(R.id.distFragmentPartsEditText)
        timeET = v.findViewById(R.id.timeFragmentPartsEditText)
        nameET = v.findViewById(R.id.nameFragmentPartsEditText)
        submitBtn = v.findViewById(R.id.submitFragmentPartsButton)
        kmCheckBox = v.findViewById(R.id.kmCheckBox)
        timeCheckBox = v.findViewById(R.id.timeCheckBox)


        //check if user uses metric units or imperial
        val db = Database(requireContext())
        isMetric = db.getIfMetric(db.readableDatabase)

        //change ET hint for imperial users :3
        if (!isMetric){
            distanceET.setHint(R.string.mi)
        }

        submitBtn.setOnClickListener {
            val name : String =  nameET.text.toString()
            if (name.isBlank()){
                Toast.makeText(requireContext(), getString(R.string.empty_fields), Toast.LENGTH_LONG).show()
            } else {
                var time = 0
                var dist = 0

                if (kmCheckBox.isChecked){
                    dist = distanceET.text.toString().toInt()
                    //if they use imperial, make them use metric :P
                    if (!isMetric){
                        dist = Calc.miToKm(dist)
                    }
                }
                if (timeCheckBox.isChecked){
                    time =  timeET.text.toString().toInt()
                }


                val cv: ContentValues = ContentValues().apply {
                    put(Database.PARTS_COLUMN_NAME, name)
                    put(Database.PARTS_COLUMN_TIME, time)
                    put(Database.PARTS_COLUMN_KM, dist)
                }
                parentActivity.fragmentPartsData(cv)
            }
        }

        return v
    }

    override fun onAutoFill(modelItem: ModelItem) {
        nameET.setText(modelItem.name)
        timeET.setText(modelItem.valueField1Text)
        distanceET.setText(modelItem.valueField2Text)
        kmCheckBox.isChecked = distanceET.text.toString().toInt() > 0
        timeCheckBox.isChecked = timeET.text.toString().toInt() > 0
    }

}