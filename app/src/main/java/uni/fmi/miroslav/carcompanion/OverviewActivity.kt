package uni.fmi.miroslav.carcompanion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uni.fmi.miroslav.carcompanion.adapters.ItemRecyclerAdapter
import uni.fmi.miroslav.carcompanion.fragments.ConfirmDeleteDialog
import uni.fmi.miroslav.carcompanion.models.ModelItem
import uni.fmi.miroslav.carcompanion.tools.Database
import java.sql.SQLException

class OverviewActivity : AppCompatActivity(), ItemRecyclerAdapter.OnItemClickListener, ConfirmDeleteDialog.DialogListener {


    private lateinit var recyclerView: RecyclerView
    private lateinit var popupMenu: PopupMenu

    private lateinit var modelItem: ModelItem
    private lateinit var arr: ArrayList<ModelItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        arr = getListItems()
        recyclerView = findViewById(R.id.recycler_view_overview_activity)

    }

    private fun updateRV(){
        val adapterRV =
            ItemRecyclerAdapter(this)
        recyclerView.apply {
            adapter = adapterRV
            layoutManager = GridLayoutManager(this@OverviewActivity, 1)
        }
        try {
            arr.remove(modelItem)
            arr.add(intent.extras?.getSerializable("model")!! as ModelItem)
        }catch (e: Exception){
            //do nothing
        }
        adapterRV.submitList(arr)
    }

    private fun getListItems(): ArrayList<ModelItem>{
        return intent.extras?.getSerializable("data")!! as ArrayList<ModelItem>
    }

    override fun onItemClick(modelItem: ModelItem, view: View) {
        this.modelItem = modelItem
        try {
            popupMenu.dismiss()
        } catch (e: UninitializedPropertyAccessException){
            //just carry on, we don't care...
        }
        createPopupMenu(modelItem, view)
    }

    private fun createPopupMenu(modelItem: ModelItem, viewV: View){
        popupMenu = PopupMenu(this, viewV)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            if (it.itemId == R.id.overview){

                val intent = Intent(this, ItemActivity::class.java)
                intent.putExtra("mode", modelItem.changeField2Text != "")
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

        if (modelItem.changeField2Text != ""){
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

    override fun onResume() {
        super.onResume()
        updateRV()
    }

    override fun pressYes(tag: String?) {
        if(deleteItem()){
            Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show()
            this.finish()
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }
}