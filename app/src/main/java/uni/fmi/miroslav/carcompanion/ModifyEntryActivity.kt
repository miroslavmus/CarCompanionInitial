package uni.fmi.miroslav.carcompanion

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uni.fmi.miroslav.carcompanion.adapters.ImageRecyclerAdapter
import uni.fmi.miroslav.carcompanion.adapters.ItemRecyclerAdapter
import uni.fmi.miroslav.carcompanion.customelements.ToggleTextView
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.interfaces.OnActivityAutoFill
import uni.fmi.miroslav.carcompanion.models.ModelImage
import uni.fmi.miroslav.carcompanion.models.ModelItem
import uni.fmi.miroslav.carcompanion.tools.Calc
import uni.fmi.miroslav.carcompanion.tools.Database
import java.sql.SQLException
import java.util.ArrayList

class ModifyEntryActivity : AppCompatActivity(),
    FragmentForm, ItemRecyclerAdapter.OnItemClickListener, ImageRecyclerAdapter.OnImageClickListener {

    private lateinit var imgIV: ImageView
    private lateinit var actBtn: FloatingActionButton
    private lateinit var popupWindow: PopupWindow
    private lateinit var attachedFragment: Fragment
    private lateinit var titleTV: ToggleTextView
    private lateinit var currentModel: ModelItem

    private lateinit var activeImage: String
    private val defaultImage = "resource_1"


    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_entry)



        //set default image
        activeImage = defaultImage

        //bind visual elements
        imgIV = findViewById(R.id.avatarModifyItemImageView)
        actBtn = findViewById(R.id.fragmentModifyActionButton)
        titleTV = ToggleTextView(findViewById(R.id.modifyEntryTextView), getString(R.string.modify_part), getString(R.string.modify_document))



        //define click listeners for the button and image view

        imgIV.setOnClickListener{ createPopUpImages(it as ImageView) }

        titleTV.tv.setOnClickListener { createPopUpItems(it as TextView) }

        if (intent.extras?.getBoolean("parts?")!! && intent.extras?.getBoolean("docs?")!!){
            titleTV.tv.setOnClickListener { titleAction1(it as TextView) }
            actBtn.setOnClickListener{ clickListenerPartsFragment() }
        }
        else if (intent.extras?.getBoolean("parts?")!!){
            actBtn.setOnClickListener{
                Toast.makeText(this, getString(R.string.docs_not_entered), Toast.LENGTH_SHORT).show()
            }
        } else if (intent.extras?.getBoolean("docs?")!!) {
            actBtn.setOnClickListener{
                Toast.makeText(this, getString(R.string.docs_not_entered), Toast.LENGTH_SHORT).show()
            }
            findNavController(R.id.modifyItemFragment).navigate(R.id.action_partFormFragment2_to_documentFormFragment2)
            titleTV.switchTo(false)
        } else {
            //cover-up case, will probably never get used, but we could get invalid arguments
            this.onBackPressed()
        }

    }
    //handling popup window for images
    @SuppressLint("InflateParams") //null params work just fine <3
    private fun createPopUpImages(it: ImageView){
        popupWindow = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.popup_item_select, null)
        popupWindow.contentView = view
        val recyclerView: RecyclerView = view.findViewById(R.id.itemSelectRecyclerView)
        val adapterRV =
            ImageRecyclerAdapter(this)
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@ModifyEntryActivity, 3)
            adapter = adapterRV
        }
        adapterRV.submitList(getListImages())
        popupWindow.showAsDropDown(it)
        it.setOnClickListener { onImageClick(activeImage) }
    }
    private fun getListImages(): ArrayList<ModelImage>{
        var counter = 1
        val arrList = ArrayList<ModelImage>()
        val string = if (titleTV.isOn){
            "resource_p"
        } else {
            "resource_d"
        }
        while (resources.getIdentifier("${string}${counter}", "drawable", this.packageName) != 0) {
            arrList.add(ModelImage("${string}${counter++}"))
        }
        return arrList
    }

    //handling popup window for items
    @SuppressLint("InflateParams")
    private fun createPopUpItems(tv: TextView){
        popupWindow = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.popup_item_select, null)
        popupWindow.contentView = view
        val recyclerView: RecyclerView = view.findViewById(R.id.itemSelectRecyclerView)
        val adapterRV =
            ItemRecyclerAdapter(this)
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@ModifyEntryActivity, 1)
            adapter = adapterRV
        }
        adapterRV.submitList(getListItems())
        popupWindow.showAsDropDown(tv)

    }
    private fun getListItems(): ArrayList<ModelItem>{

        val arrList = ArrayList<ModelItem>()

        val myDb = Database(this)
        val db = myDb.readableDatabase
        var cursor: Cursor? = null
        try {
            val query: String
            if (titleTV.isOn){
                query = "SELECT ${Database.PARTS_COLUMN_NAME}, ${Database.PARTS_COLUMN_PIC}, ${Database.PARTS_COLUMN_KM}, ${Database.PARTS_COLUMN_TIME}, ${Database.PARTS_COLUMN_ID} " +
                        "FROM ${Database.TABLE_PARTS}"
                cursor = db.rawQuery(query, null)

                if (!cursor.moveToFirst()) return arrList

                do{
                    var divider: Double
                    var dist: String
                    if(!myDb.getIfMetric(myDb.readableDatabase)){
                        divider = Calc.KM_MI_RATIO
                        dist = getString(R.string.mi)
                    } else {
                        dist = getString(R.string.km)
                        divider = 1.0
                    }
                    arrList.add(ModelItem(
                        cursor.getString(1),
                        cursor.getString(0),
                        getString(R.string.change_every) + dist,
                        getString(R.string.change_every) + getString(R.string.months),
                        (cursor.getInt(2)/divider).toInt().toString(),
                        cursor.getString(3),
                        "",
                        cursor.getInt(4)))
                } while (cursor.moveToNext())
            } else {
                query = "SELECT ${Database.DOCUMENTS_COLUMN_NAME}, ${Database.DOCUMENTS_COLUMN_PIC}, ${Database.DOCUMENTS_COLUMN_TODATE}, ${Database.DOCUMENTS_COLUMN_ID} " +
                        "FROM ${Database.TABLE_DOCUMENTS}"

                cursor = db.rawQuery(query, null)

                if (!cursor.moveToFirst()) return arrList

                do {
                    arrList.add( ModelItem(
                        cursor.getString(1),
                        cursor.getString(0),
                        getString(R.string.expires_on),
                        "",
                        cursor.getString(2),
                        "",
                        "",
                        cursor.getInt(3)))

                } while (cursor.moveToNext())
            }

        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db.close()
            cursor?.close()
        }
        return arrList
    }

    //title actions
    private fun titleAction1(tv: TextView){
        createPopUpItems(tv)
        tv.setOnClickListener { titleAction2(tv) }
    }
    private fun titleAction2(tv: TextView){
        popupWindow.dismiss()
        tv.setOnClickListener { titleAction1(tv) }
    }

    //click listeners for switcher action button
    private fun clickListenerPartsFragment(){
        findNavController(R.id.modifyItemFragment).navigate(R.id.action_partFormFragment2_to_documentFormFragment2)
        titleTV.toggle()
        try { popupWindow.dismiss() }
        catch (e: UninitializedPropertyAccessException){
            //we don't care.....
        }
        if (defaultImage != activeImage){
            activeImage = defaultImage
            imgIV.setImageResource(resources.getIdentifier(activeImage, "drawable", this.packageName))
        }
        actBtn.setOnClickListener { clickListenerDocumentFragment() }
    }
    private fun clickListenerDocumentFragment(){
        findNavController(R.id.modifyItemFragment).navigate(R.id.action_documentFormFragment2_to_partFormFragment2)
        titleTV.toggle()
        popupWindow.dismiss()
        if (defaultImage != activeImage){
            activeImage = defaultImage
            imgIV.setImageResource(resources.getIdentifier(activeImage, "drawable", this.packageName))
        }
        actBtn.setOnClickListener { clickListenerPartsFragment() }
    }

    //overrides (interface)
    override fun onImageClick(image: String) {
        if (activeImage != image) {
            activeImage = image
            imgIV.setImageResource(resources.getIdentifier(activeImage, "drawable", this.packageName))
        }
        popupWindow.dismiss()
        imgIV.setOnClickListener { createPopUpImages(it as ImageView) }
    }
    override fun onItemClick(modelItem: ModelItem, view: View) {
        currentModel = modelItem
        activeImage = modelItem.picturePath
        imgIV.setImageResource(resources.getIdentifier(activeImage, "drawable", this.packageName))

        (attachedFragment as OnActivityAutoFill).onAutoFill(modelItem)
        popupWindow.dismiss()
        titleTV.tv.setOnClickListener{
            titleAction1(it as TextView)
        }
    }

    //attach our fragment
    override fun onFragmentAttach(fragment: Fragment) {
        attachedFragment = fragment
    }

    //updates interfaces
    override fun fragmentDocumentsData(contentValues: ContentValues) {
        val myDb = Database(this)
        val db = myDb.writableDatabase
        var affected = 0
        db.beginTransaction()
        try {
            contentValues.put(Database.DOCUMENTS_COLUMN_PIC, activeImage)
            affected = db.update(Database.TABLE_DOCUMENTS, contentValues, "${Database.DOCUMENTS_COLUMN_ID} = ?", arrayOf(currentModel.id.toString()))
            if (affected == 1) {
                db.setTransactionSuccessful()
            } else {
                affected = 0
            }
        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
        closeOnSuccess(affected)
    }
    override fun fragmentPartsData(contentValues: ContentValues) {
        val myDb = Database(this)
        var db: SQLiteDatabase? = null
        var affected = 0
        try {
            db = myDb.writableDatabase
            db.beginTransaction()
            if (!myDb.getIfMetric(db)){
                val mi = contentValues.getAsInteger(Database.PARTS_COLUMN_KM)
                contentValues.remove(Database.PARTS_COLUMN_KM)
                contentValues.put(Database.PARTS_COLUMN_KM, Calc.miToKm(mi))
            }
            contentValues.put(Database.PARTS_COLUMN_PIC, activeImage)
            affected = db.update(Database.TABLE_PARTS, contentValues, "${Database.PARTS_COLUMN_ID} = ?", arrayOf(currentModel.id.toString()))
            if (affected == 1) {
                db.setTransactionSuccessful()
            } else {
                affected = 0
            }
            db.endTransaction()
        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db?.close()
        }
        closeOnSuccess(affected)
    }

    //update success checker
    private fun closeOnSuccess(success: Int){
        if (success == 0){
            Toast.makeText(this, getString(R.string.invalid_reentry), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.sucess_reentry), Toast.LENGTH_SHORT).show()
            this.finish()
        }
    }

    //override onBackPressed to do something more useful
    override fun onBackPressed() {
        Toast.makeText(this, getString(R.string.changes_discarded), Toast.LENGTH_SHORT).show()
        this.finish()
    }


}

