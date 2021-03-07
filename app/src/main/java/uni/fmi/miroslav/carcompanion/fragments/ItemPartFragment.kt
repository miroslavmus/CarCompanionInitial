package uni.fmi.miroslav.carcompanion.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uni.fmi.miroslav.carcompanion.tools.Database
import uni.fmi.miroslav.carcompanion.adapters.FixRecyclerAdapter
import uni.fmi.miroslav.carcompanion.ItemActivity
import uni.fmi.miroslav.carcompanion.R
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelFix
import uni.fmi.miroslav.carcompanion.models.ModelItem
import java.sql.SQLException
import java.util.Map.entry

class ItemPartFragment : Fragment(), OnActivityAutoFill, FixRecyclerAdapter.OnFixClickListener, ItemActivity.FixDelete {

    private lateinit var modelItem: ModelItem
    private lateinit var modelFix: ModelFix
    private lateinit var listener: FragmentForm

    private lateinit var recyclerView: RecyclerView
    private lateinit var popupMenu: PopupMenu
    private lateinit var delPartBtn: Button
    private lateinit var delEntBtn: Button
    private lateinit var addFixBtn: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as FragmentForm
        listener.onFragmentAttach(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_item_part, container, false)

        modelItem = (listener as Activity).intent.extras?.getSerializable("model") as ModelItem

        recyclerView = v.findViewById(R.id.fixRecyclerView)
        delEntBtn = v.findViewById(R.id.deleteDataButton)
        delPartBtn = v.findViewById(R.id.deletePartButton)
        addFixBtn = v.findViewById(R.id.addFixButton)

        updateRV()

        delEntBtn.setOnClickListener {
            (listener as ItemActivity).dialog =
                ConfirmDeleteDialog(
                    (listener as Activity).getString(R.string.confirm_delete_of) + (listener as Activity).getString(
                        R.string.data
                    ) + '?',
                    (listener as Activity).getString(R.string.delete),
                    (listener as Activity).getString(R.string.reject)
                )
            (listener as ItemActivity).dialog!!.show(parentFragmentManager, "itemPartFix")
        }

        delPartBtn.setOnClickListener {
            (listener as ItemActivity).dialog =
                ConfirmDeleteDialog(
                    (listener as Activity).getString(R.string.confirm_delete_of) + modelItem.name + '?',
                    (listener as Activity).getString(R.string.delete),
                    (listener as Activity).getString(R.string.reject)
                )
            (listener as ItemActivity).dialog!!.show(parentFragmentManager, "itemPart")
        }

        addFixBtn.setOnClickListener {
            (listener as ItemActivity).findNavController(
                R.id.fragmentData
            ).navigate(R.id.action_itemPartFragment_to_createFixFragment)
        }

        return v
    }

    override fun onAutoFill(modelItem: ModelItem) {
        this.modelItem = modelItem
    }
    fun updateRV(){
        val adapterRV =
            FixRecyclerAdapter(
                this,
                requireContext()
            )
        recyclerView.apply {
            adapter = adapterRV
            layoutManager = GridLayoutManager(requireContext(), 1)
        }
        adapterRV.submitList(getFixList())
    }
    private fun getFixList(): ArrayList<ModelFix>{
        val arr = ArrayList<ModelFix>()

        val myDb = Database(requireContext())
        val db = myDb.readableDatabase
        var cursor: Cursor? = null
        try {
            val query = "SELECT ${Database.FIX_COLUMN_PART_ID}, ${Database.FIX_COLUMN_KM}, ${Database.FIX_COLUMN_DATE}, ${Database.FIX_COLUMN_INFO} FROM ${Database.TABLE_FIX} WHERE ${Database.FIX_COLUMN_PART_ID} = ${modelItem.id} ORDER BY ${Database.FIX_COLUMN_KM} DESC"
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()){
                do {
                    arr.add(
                        ModelFix(
                            partId = cursor.getInt(0),
                            km = cursor.getInt(1),
                            date = cursor.getString(2),
                            message = cursor.getString(3)
                    ))
                } while (cursor.moveToNext())
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.close()
            cursor?.close()
        }

        return arr
    }



    override fun onFixClick(modelItem: ModelFix, view: View) {
        try {
            popupMenu.dismiss()
        } catch (e: UninitializedPropertyAccessException){
            //just carry on, we don't care...
        }
        createPopupMenu(modelItem, view)
    }
    private fun createPopupMenu(modelItem: ModelFix, view: View){
        this.modelFix = modelItem
        popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            if (it.itemId == R.id.overview){
                val dialog =
                        ConfirmDeleteDialog(
                                "${getString(R.string.message)}\n ${modelFix.message}",
                                "",
                                getString(R.string.ok)
                        )
                dialog.show(parentFragmentManager, "NoOK")
            } else {
                //delete entry
                val dialog =
                        ConfirmDeleteDialog(
                                "${getString(R.string.confirm_delete_of)} ${getString(R.string.entry)}  ${getString(R.string.changed_at_date)} ${modelItem.date}?",
                                getString(R.string.delete),
                                getString(R.string.reject)
                        )
                dialog.show(parentFragmentManager, "deleteEntryFix")
            }
            popupMenu.dismiss()
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }



    override fun onResume() {
        super.onResume()
        updateRV()
    }

    override fun deleteFixEntry() {
        val myDb = Database(requireContext())
        var db: SQLiteDatabase? = null
        try {
            db = myDb.writableDatabase
            val query = "DELETE FROM ${Database.TABLE_FIX} WHERE ${Database.FIX_COLUMN_PART_ID} = ${modelItem.id} AND ${Database.FIX_COLUMN_KM} = ${modelFix.km}"
            db.execSQL(query)
        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db?.close()
        }
        updateRV()
    }

}

