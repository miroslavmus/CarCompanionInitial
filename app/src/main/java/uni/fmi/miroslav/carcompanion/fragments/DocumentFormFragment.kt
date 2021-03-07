package uni.fmi.miroslav.carcompanion.fragments

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import uni.fmi.miroslav.carcompanion.tools.Database
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.customelements.CustomDatePicker
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelItem


class DocumentFormFragment : Fragment(),
    OnActivityAutoFill {


    private lateinit var nameET: EditText
    private lateinit var dateET: EditText
    private lateinit var submitBtn: Button

    private lateinit var datePicker: CustomDatePicker

    //attach to activity for communication
    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as FragmentForm
        parentActivity.onFragmentAttach(this)
    }

    private lateinit var parentActivity: FragmentForm

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_document_form, container, false)

        submitBtn = v.findViewById(R.id.submitFragmentDocsButton)
        nameET = v.findViewById(R.id.nameFragmentDocsEditText)
        dateET = v.findViewById(R.id.dateFragmentDocsEditText)

        dateET.setOnClickListener { }
        datePicker =
            CustomDatePicker(
                requireContext(),
                dateET
            )

        submitBtn.setOnClickListener {
            val name : String =  nameET.text.toString().trim()
            val date = dateET.text.toString().trim()
            if (name.isBlank() || date.isBlank()){
                Toast.makeText(requireContext(), getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            } else {
                val cv: ContentValues = ContentValues().apply {
                    put(Database.DOCUMENTS_COLUMN_NAME, name)
                    put(Database.DOCUMENTS_COLUMN_TODATE, date)
                }
                parentActivity.fragmentDocumentsData(cv)
            }
        }

        return v
    }

    override fun onAutoFill(modelItem: ModelItem) {
        nameET.setText(modelItem.name)
        dateET.setText(modelItem.valueField1Text)
    }
}