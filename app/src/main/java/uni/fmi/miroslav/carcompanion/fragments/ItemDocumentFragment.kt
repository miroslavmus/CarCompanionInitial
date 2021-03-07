package uni.fmi.miroslav.carcompanion.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import uni.fmi.miroslav.carcompanion.tools.Database
import uni.fmi.miroslav.carcompanion.ItemActivity
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.customelements.CustomDatePicker
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelItem
import java.sql.SQLException

class ItemDocumentFragment : Fragment(),
    OnActivityAutoFill {

    private lateinit var date: EditText
    private lateinit var delBtn: Button
    private lateinit var modBtn: Button

    private lateinit var modelItem: ModelItem
    private lateinit var datePicker: CustomDatePicker


    private lateinit var listener: FragmentForm
    //attach our activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as FragmentForm
        listener.onFragmentAttach(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_item_document, container, false)

        delBtn = v.findViewById(R.id.deleteDocumentButton)
        modBtn = v.findViewById(R.id.renewDocumentButton)

        date = v.findViewById(R.id.dateEditText)

        modelItem = (listener as ItemActivity).intent.extras?.get("model")!! as ModelItem

        delBtn.setOnClickListener{
            (listener as ItemActivity).dialog =
                ConfirmDeleteDialog(
                    getString(R.string.confirm_delete_of) + modelItem.name + '?',
                    getString(R.string.delete),
                    getString(R.string.reject)
                )
            (listener as ItemActivity).dialog?.show(parentFragmentManager, "itemDocument")
        }

        modBtn.setOnClickListener{
            if (date.text.toString().isBlank()){
                return@setOnClickListener
            }
            val myDb =
                Database(listener as Activity)
            val db = myDb.writableDatabase
            val contentValues = ContentValues()
            val affected: Int
            try {
                db.beginTransaction()
                contentValues.put(Database.DOCUMENTS_COLUMN_TODATE, date.text.toString())
                affected = db.update(
                    Database.TABLE_DOCUMENTS,
                    contentValues,
                    "${Database.DOCUMENTS_COLUMN_ID} = ?",
                    arrayOf(modelItem.id.toString()))
                if (affected == 1) {
                    db.setTransactionSuccessful()
                    (listener as Activity).finish()
                }
            } catch (e: SQLException){
                e.printStackTrace()
            } finally {
                db.endTransaction()
                db.close()
            }

        }
        datePicker =
            CustomDatePicker(
                listener as Activity,
                date
            )

        return v
    }

    override fun onAutoFill(modelItem: ModelItem) {
        this.modelItem = modelItem
        date.setText(modelItem.valueField2Text)
    }




}