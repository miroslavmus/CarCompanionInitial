package uni.fmi.miroslav.carcompanion

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uni.fmi.miroslav.carcompanion.adapters.ImageRecyclerAdapter
import uni.fmi.miroslav.carcompanion.customelements.ToggleTextView
import uni.fmi.miroslav.carcompanion.interfaces.FragmentForm
import uni.fmi.miroslav.carcompanion.models.ModelImage
import uni.fmi.miroslav.carcompanion.tools.Database
import java.sql.SQLException
import java.util.ArrayList

class CreateEntryActivity : AppCompatActivity(),
    FragmentForm, ImageRecyclerAdapter.OnImageClickListener{

    private lateinit var imgIV: ImageView
    private lateinit var actBtn: FloatingActionButton
    private lateinit var activeImage: String
    private lateinit var popupWindow: PopupWindow
    private lateinit var attachedFragment: Fragment
    private lateinit var titleTV: ToggleTextView

    private val defaultImage = "resource_1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_entry)

        //set default image
        activeImage = defaultImage
        popupWindow = PopupWindow(this)

        //bind visual elements
        imgIV = findViewById(R.id.avatarCreateItemImageView)
        actBtn = findViewById(R.id.fragmentCreateActionButton)
        titleTV = ToggleTextView(findViewById(R.id.createEntryTextView), getString(R.string.create_new_part), getString(R.string.create_new_document))

        //define click listeners for the button and image view
        actBtn.setOnClickListener{ clickListenerPartsFragment() }

        imgIV.setOnClickListener{ createPopUpImages(it as ImageView) }


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
            layoutManager = GridLayoutManager(this@CreateEntryActivity, 3)
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

    //click listeners for switcher action button
    private fun clickListenerPartsFragment(){
        findNavController(R.id.createItemFragment).navigate(R.id.action_partFormFragment_to_documentFormFragment)
        titleTV.toggle()
        popupWindow.dismiss()
        if (defaultImage != activeImage){
            activeImage = defaultImage
            imgIV.setImageResource(resources.getIdentifier(activeImage, "drawable", this.packageName))
        }
        actBtn.setOnClickListener { clickListenerDocumentFragment() }
    }
    private fun clickListenerDocumentFragment(){
        findNavController(R.id.createItemFragment).navigate(R.id.action_documentFormFragment_to_partFormFragment)
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
        imgIV.setOnClickListener { createPopUpImages(imgIV) }
    }

    //attach our fragment
    override fun onFragmentAttach(fragment: Fragment) {
        attachedFragment = fragment
    }

    //inserts interfaces
    override fun fragmentPartsData(contentValues: ContentValues) {
        val myDb = Database(this)
        var db : SQLiteDatabase? = null

        if (activeImage == defaultImage){
            Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
            return
        }

        var success = 0L
        try {
            db = myDb.writableDatabase
            contentValues.put(Database.PARTS_COLUMN_PIC, activeImage)
            success = db.insert(Database.TABLE_PARTS, null, contentValues)
        } catch (e: SQLiteConstraintException) {
            Toast.makeText(this, getString(R.string.unique_names_exception), Toast.LENGTH_LONG).show()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db?.close()
        }
        closeOnSuccess(success)
    }
    override fun fragmentDocumentsData(contentValues: ContentValues) {
        val myDb = Database(this)
        var db : SQLiteDatabase? = null

        if (activeImage == defaultImage){
            Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
            return
        }

        var success = 0L
        try {
            db = myDb.writableDatabase
            contentValues.put(Database.DOCUMENTS_COLUMN_PIC, activeImage)
            success = db.insert(Database.TABLE_DOCUMENTS, null, contentValues)
        } catch (e: SQLiteConstraintException) {
            Toast.makeText(this, getString(R.string.unique_names_exception), Toast.LENGTH_LONG).show()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db?.close()
        }
        closeOnSuccess(success)
    }

    //insert success checker
    private fun closeOnSuccess(success: Long){
        if (success == -1L){
            Toast.makeText(this, getString(R.string.invalid_entry), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, getString(R.string.sucess_entry), Toast.LENGTH_LONG).show()
            this.finish()
        }
    }

    //override onBackPressed to do something more useful
    override fun onBackPressed() {
        Toast.makeText(this, getString(R.string.changes_discarded), Toast.LENGTH_SHORT).show()
        this.finish()
    }

}