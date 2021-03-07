package uni.fmi.miroslav.carcompanion

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import uni.fmi.miroslav.carcompanion.tools.Database
import uni.fmi.miroslav.carcompanion.customelements.ToggleButton
import uni.fmi.miroslav.carcompanion.fragments.ConfirmDeleteDialog

class SettingsActivity : AppCompatActivity(), ConfirmDeleteDialog.DialogListener {

    private lateinit var tglBtn: ToggleButton

    private lateinit var tutorialBtn: Button
    private lateinit var toGPBtn: Button
    private lateinit var infoBtn: Button
    private lateinit var clearBtn: Button
    private lateinit var wipeBtn: Button
    private lateinit var aboutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //bind all visual elements and set click listeners
        tutorialBtn = findViewById(R.id.tutorialButton)
        toGPBtn = findViewById(R.id.toPlayStoreButton)
        infoBtn = findViewById(R.id.InfoButton)
        clearBtn = findViewById(R.id.ClearButton)
        wipeBtn = findViewById(R.id.WipeButton)
        aboutBtn = findViewById(R.id.AboutButton)

        tutorialBtn.setOnClickListener {
            toGooglePlay()
            //ConfirmDeleteDialog(getString(R.string.tutorial_dialog_settings),  getString(R.string.ok), getString(R.string.reject)).show(supportFragmentManager, "tutorial")
        }
        toGPBtn.setOnClickListener { toGooglePlay() }
        infoBtn.setOnClickListener { ConfirmDeleteDialog(getString(R.string.information_dialog_settings), "", getString(R.string.ok)).show(supportFragmentManager, "info") }
        clearBtn.setOnClickListener { ConfirmDeleteDialog(getString(R.string.confirm_clear_data_settings), getString(R.string.delete), getString(R.string.reject)).show(supportFragmentManager, "clear") }
        wipeBtn.setOnClickListener { ConfirmDeleteDialog(getString(R.string.confirm_wipe_data_settings), getString(R.string.delete), getString(R.string.reject)).show(supportFragmentManager, "wipe") }
        aboutBtn.setOnClickListener { ConfirmDeleteDialog(getString(R.string.about_app_dialog_settings), "", getString(R.string.ok)).show(supportFragmentManager, "about") }

        //toggle button for metric systems
        tglBtn = ToggleButton(
            findViewById(R.id.unitsButton),
            getString(R.string.metric),
            getString(R.string.imperial)
        )
        tglBtn.btn.setOnClickListener{ toggleMetric() }

    }

    private fun syncMetric(){
        var db : SQLiteDatabase? = null
        var cursor: Cursor? = null

        try {
            val myDb = Database(this)
            db = myDb.readableDatabase
            cursor = db.rawQuery("SELECT ${Database.SETTINGS_COLUMN_METRIC} FROM ${Database.TABLE_SETTINGS}",null)
            cursor.moveToFirst()

            tglBtn.switchTo(cursor.getInt(0) == 1)

        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db?.close()
            cursor?.close()
        }
    }
    private fun toggleMetric(){
        var db : SQLiteDatabase? = null

        tglBtn.toggle()

        try {
            val myDb = Database(this)
            db = myDb.writableDatabase
            val isMetric: Int = when {
                tglBtn.isOn -> {
                    1
                }
                else -> 0
            }

            val rows = db.update(Database.TABLE_SETTINGS, ContentValues().apply { put(
                Database.SETTINGS_COLUMN_METRIC, isMetric) },null, null)

            if (rows > 1) Toast.makeText(this, "CATASTROPHIC DATABASE FAILURE: YOU HAVE 2 SETTINGS ENTRIES!!!", Toast.LENGTH_LONG).show()
        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            db?.close()
        }

    }

    private fun toGooglePlay(){
        Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        this.finish()
    }

    //enforce unit sync
    override fun onResume() {
        super.onResume()
        syncMetric()
    }

    override fun pressYes(tag: String?) {
        val myDb = Database(this)
        when (tag){
            "clear" -> myDb.onDataWipe(myDb.writableDatabase)
            "wipe" -> myDb.onDataWipe(myDb.writableDatabase, true)
            "tutorial" -> startActivity(Intent(this, TutorialActivity::class.java))
        }
    }
}