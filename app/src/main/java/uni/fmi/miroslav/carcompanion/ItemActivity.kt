package uni.fmi.miroslav.carcompanion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import uni.fmi.miroslav.carcompanion.fragments.*
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelItem
import uni.fmi.miroslav.carcompanion.tools.Database
import java.sql.SQLException

class ItemActivity : AppCompatActivity(),
    FragmentForm, ConfirmDeleteDialog.DialogListener  {

    private lateinit var fragmentItem: OnActivityAutoFill
    private lateinit var fragmentData: OnActivityAutoFill
    private lateinit var modelItem: ModelItem
    private var isPart: Boolean = true

    var dialog: ConfirmDeleteDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        isPart = intent.extras?.getBoolean("mode", true)!!
        modelItem = intent.extras?.getSerializable("model")!! as ModelItem

        fragmentItem.onAutoFill(modelItem)
        fragmentData.onAutoFill(modelItem)

        if (!isPart){
            findNavController(R.id.fragmentData).navigate(R.id.action_itemPartFragment_to_itemDocumentFragment)
        }

    }

    override fun onFragmentAttach(fragment: Fragment) {
        if (fragment is ItemFragment){
            fragmentItem = fragment
        } else {
            fragmentData = fragment as OnActivityAutoFill
        }
    }


    override fun pressYes(tag: String?) {
        when (tag){
            "itemDocument" ->
                deleteItem()
            "itemPart" ->
                deleteItem(Database.TABLE_FIX)
            "itemPartFix" ->
                deleteItem(Database.TABLE_PARTS)
            "deleteEntryFix" ->
                (fragmentData as ItemPartFragment).deleteFixEntry()
        }
    }

    private fun deleteItem(table: String = ""){
        val myDb = Database(this)
        val db = myDb.writableDatabase
        try {
            val query= if (table.isEmpty()){
                "DELETE FROM $table WHERE ${if (table == Database.TABLE_FIX) Database.FIX_COLUMN_PART_ID else Database.PARTS_COLUMN_ID} = ${modelItem.id}"
            } else {
                "DELETE FROM ${Database.TABLE_DOCUMENTS} WHERE ${Database.DOCUMENTS_COLUMN_ID} = ${modelItem.id}"
            }

            db.execSQL(query)
        } catch (e: SQLException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show()
            return
        } finally {
            db.close()
        }
        if (table == Database.TABLE_PARTS){
            this.finish()
        } else {
            (fragmentData as ItemPartFragment).updateRV()
        }
        intent.removeExtra("model")
        Toast.makeText( this, getString(R.string.success), Toast.LENGTH_SHORT).show()
    }

    interface FixDelete
    {
        fun deleteFixEntry()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}