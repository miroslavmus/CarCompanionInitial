package uni.fmi.miroslav.carcompanion

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uni.fmi.miroslav.carcompanion.adapters.ItemRecyclerAdapter
import uni.fmi.miroslav.carcompanion.customelements.ToggleTextView
import uni.fmi.miroslav.carcompanion.fragments.ConfirmDeleteDialog
import uni.fmi.miroslav.carcompanion.models.ModelItem
import uni.fmi.miroslav.carcompanion.tools.Calc
import uni.fmi.miroslav.carcompanion.tools.Database
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList


class ItemsOverviewActivity : AppCompatActivity(), ItemRecyclerAdapter.OnItemClickListener, ConfirmDeleteDialog.DialogListener {

    private lateinit var titleTV: ToggleTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var popupMenu: PopupMenu

    private lateinit var modelItem: ModelItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_overview)

        titleTV = ToggleTextView(findViewById(R.id.itemTextView), getString(R.string.existing_parts), getString(R.string.existing_documents))

        when (intent.extras?.get("mode")!!){
            ("doc") -> titleTV.switchTo(false)
            ("part") -> titleTV.switchTo(true)
            else -> this.onBackPressed()
        }

        recyclerView = findViewById(R.id.itemOverview)
        updateRV()
    }

    private fun updateRV(){
        val adapterRV =
            ItemRecyclerAdapter(this)
        recyclerView.apply {
            adapter = adapterRV
            layoutManager = GridLayoutManager(this@ItemsOverviewActivity, 1)
        }
        adapterRV.submitList(getListItems())
    }

    @SuppressLint("SimpleDateFormat")
    private fun getListItems(): ArrayList<ModelItem> {

        val arrList = ArrayList<ModelItem>()

        val myDb = Database(this)
        val db = myDb.readableDatabase
        var cursor: Cursor? = null
        try {
            val query: String
            val sdf = SimpleDateFormat("yyyy/MM/dd")
            if (titleTV.isOn){
                query = "SELECT ${Database.PARTS_COLUMN_NAME}, ${Database.PARTS_COLUMN_PIC}, ${Database.PARTS_COLUMN_KM}, ${Database.PARTS_COLUMN_TIME}, ${Database.PARTS_COLUMN_ID} " +
                        "FROM ${Database.TABLE_PARTS}"
                cursor = db.rawQuery(query, null)

                if (!cursor.moveToFirst()) return arrList

                var dist = getString(R.string.km)
                var mult = 1.0
                if (!myDb.getIfMetric(myDb.readableDatabase)){
                    dist = getString(R.string.mi)
                    mult = Calc.KM_MI_RATIO
                }

                do{
                    val model =
                        ModelItem(
                        cursor.getString(1),
                        cursor.getString(0),
                        getString(R.string.change_every) + dist,
                        getString(R.string.change_every) + getString(R.string.months),
                        (cursor.getInt(2) / mult).toInt().toString(),
                        cursor.getString(3),
                        "",
                        cursor.getInt(4))

                    if (model.valueField2Text.toInt() != 0) {
                        val tempDb = myDb.readableDatabase
                        val c = tempDb.rawQuery("SELECT ${Database.FIX_COLUMN_PART_ID}, ${Database.FIX_COLUMN_DATE} FROM ${Database.TABLE_FIX} WHERE ${Database.FIX_COLUMN_PART_ID} = ${model.id}", null)
                        if (c.moveToFirst()){
                            val modelDate = sdf.parse(c.getString(1))
                            val dueDate = sdf.parse(sdf.format(StartActivity.getMonthsBefore(model.valueField2Text.toInt())))
                            if (dueDate!! > modelDate){
                                model.message = getString(R.string.part_warning)
                            } else if (sdf.parse(sdf.format(StartActivity.getMonthsBefore(model.valueField2Text.toInt() - 1)))!! > modelDate){
                                model.message = getString(R.string.part_attention)
                            }
                        }
                        c.close()
                    }
                    arrList.add(model)
                } while (cursor.moveToNext())
            } else {
                query = "SELECT ${Database.DOCUMENTS_COLUMN_NAME}, ${Database.DOCUMENTS_COLUMN_PIC}, ${Database.DOCUMENTS_COLUMN_TODATE}, ${Database.DOCUMENTS_COLUMN_ID} " +
                        "FROM ${Database.TABLE_DOCUMENTS}"

                cursor = db.rawQuery(query, null)

                if (!cursor.moveToFirst()) return arrList
                val today = sdf.parse(sdf.format(Date()))
                val compareDate = StartActivity.getDaysAgo(-14)

                do {
                   val model = ModelItem(
                        cursor.getString(1),
                        cursor.getString(0),
                        getString(R.string.expires_on),
                        "",
                        cursor.getString(2),
                        "",
                        "",
                        cursor.getInt(3))
                    val modelDate = sdf.parse(model.valueField1Text)
                    if (modelDate!! < today){
                        model.message = getString(R.string.document_warning)
                    } else if (modelDate < compareDate){
                        model.message = getString(R.string.document_attention)
                    }
                arrList.add(model)
                } while (cursor.moveToNext())
            }

        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db.close()
            cursor?.close()
        }

        //put the ones with messages on the top
        val arr =  ArrayList<ModelItem>()
        var i = 0
        while (arrList.size != i + 1){
            if (arrList[i].message == ""){
                arr.add(arrList[i])
                arrList.remove(arrList[i])
            } else i++
        }
        arrList.addAll(arr)

        return arrList
    }

    override fun onItemClick(modelItem: ModelItem, view: View) {
        try {
            popupMenu.dismiss()
        } catch (e: UninitializedPropertyAccessException){
            //just carry on, we don't care...
        }
        createPopupMenu(modelItem, view)
    }
    private fun createPopupMenu(modelItem: ModelItem, viewV: View){
        this.modelItem = modelItem
        popupMenu = PopupMenu(this, viewV)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            if (it.itemId == R.id.overview){

                val intent = Intent(this, ItemActivity::class.java)
                intent.putExtra("mode", titleTV.isOn)
                intent.putExtra("model", modelItem)
                startActivity(intent)

            } else {
                //delete entry
                val dialog =
                    ConfirmDeleteDialog(
                        getString(R.string.confirm_delete_of) + modelItem.name + '?',
                        getString(R.string.delete),
                        getString(R.string.reject)
                    )
                dialog.show(supportFragmentManager, "deleteDialogTag")
            }
            popupMenu.dismiss()
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }
    private fun deleteItem(): Boolean{
        val myDb = Database(this)
        val db = myDb.writableDatabase
        val table: String
        val id: String

        if (titleTV.isOn){
            table = Database.TABLE_PARTS
            id = Database.PARTS_COLUMN_ID
        } else {
            id = Database.DOCUMENTS_COLUMN_ID
            table = Database.TABLE_DOCUMENTS
        }

        try {
            val query = "DELETE FROM $table WHERE $id = ${modelItem.id}"
            db.execSQL(query)
        } catch (e: SQLException){
            e.printStackTrace()
            return false
        } finally {
            db.close()
        }
        return true
    }

    override fun pressYes(tag: String?) {
        if(deleteItem()){
            Toast.makeText(this, getString(R.string.success), Toast.LENGTH_LONG).show()
            updateRV()
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onResume() {
        super.onResume()
        updateRV()
    }


}