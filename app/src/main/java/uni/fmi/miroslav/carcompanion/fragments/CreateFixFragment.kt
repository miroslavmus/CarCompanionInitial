package uni.fmi.miroslav.carcompanion.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.findNavController
import uni.fmi.miroslav.carcompanion.tools.Calc
import uni.fmi.miroslav.carcompanion.tools.Database
import uni.fmi.miroslav.carcompanion.ItemActivity
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelItem
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date

class CreateFixFragment : Fragment(),
    OnActivityAutoFill {

    private lateinit var kmET: EditText
    private lateinit var commentET: EditText
    private lateinit var submitBtn: Button
    private lateinit var cancelBtn: Button

    private lateinit var modelItem: ModelItem
    private lateinit var listener: FragmentForm

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_create_fix, container, false)

        kmET = v.findViewById(R.id.kmFixEditText)
        commentET = v.findViewById(R.id.messageFixEditText)
        submitBtn = v.findViewById(R.id.submitFixButton)
        cancelBtn = v.findViewById(R.id.cancelFixButton)

        modelItem = (listener as ItemActivity).intent.extras?.get("model")!! as ModelItem

        val myDb = Database(requireContext())
        if (!myDb.getIfMetric(myDb.readableDatabase)){
            kmET.setHint(R.string.mi)
        }

        submitBtn.setOnClickListener {
            if (!kmET.text.isBlank()) {
                var db: SQLiteDatabase? = null
                var cursor: Cursor? = null
                try {
                    db = myDb.readableDatabase
                    val query = "SELECT ${Database.FIX_COLUMN_PART_ID}, ${Database.FIX_COLUMN_KM} FROM ${Database.TABLE_FIX} " +
                            "WHERE ${Database.FIX_COLUMN_PART_ID} = ${modelItem.id} ORDER BY ${Database.FIX_COLUMN_KM} DESC"
                    cursor = db.rawQuery(query, null)
                    if (cursor.moveToFirst()){
                        if (kmET.text.toString().toInt() <= cursor.getInt(1)){
                            Toast.makeText(requireContext(), getString(R.string.invalid_entry), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }
                }catch (e: SQLException){
                    e.printStackTrace()
                } finally {
                    db?.close()
                    cursor?.close()
                }
                try {
                    val contentValues = ContentValues().apply {
                        val sdf = SimpleDateFormat("yyyy/MM/dd")
                        val strDate = sdf.format(Date())
                            put(
                                Database.FIX_COLUMN_KM,
                                (if (myDb.getIfMetric(myDb.readableDatabase)) kmET.text.toString() else Calc.miToKm(
                                    kmET.text.toString().toInt()
                                ).toString())
                            )
                            put(Database.FIX_COLUMN_DATE, strDate)
                            put(Database.FIX_COLUMN_INFO, commentET.text.toString())
                            put(Database.FIX_COLUMN_PART_ID, modelItem.id)
                        }
                    db = myDb.writableDatabase
                    val success = db.insert(Database.TABLE_FIX, null, contentValues)
                    if (success == -1L) {
                        throw SQLException("THE INSERT FAILED")
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                } finally {
                    db?.close()
                }
                (listener as ItemActivity).findNavController(
                    R.id.fragmentData
                ).navigate(R.id.action_createFixFragment_to_itemPartFragment)
            }
        }

        cancelBtn.setOnClickListener {
            (listener as ItemActivity).findNavController(
                R.id.fragmentData
            ).navigate(R.id.action_createFixFragment_to_itemPartFragment)
        }

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as FragmentForm
    }

    override fun onAutoFill(modelItem: ModelItem) {
        this.modelItem = modelItem
    }


}