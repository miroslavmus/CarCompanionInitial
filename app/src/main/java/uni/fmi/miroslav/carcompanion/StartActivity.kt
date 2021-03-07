package uni.fmi.miroslav.carcompanion

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import uni.fmi.miroslav.carcompanion.models.ModelItem
import uni.fmi.miroslav.carcompanion.tools.Calc
import uni.fmi.miroslav.carcompanion.tools.Database
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import kotlin.collections.ArrayList

class StartActivity : AppCompatActivity() {

    private lateinit var create: ConstraintLayout
    private lateinit var calculator: ConstraintLayout
    private lateinit var overview: ConstraintLayout
    private lateinit var update: ConstraintLayout
    private lateinit var documents: ConstraintLayout
    private lateinit var parts: ConstraintLayout
    private lateinit var settings: ConstraintLayout

    private lateinit var partAtt:TextView
    private lateinit var partWarn:TextView
    private lateinit var docAtt:TextView
    private lateinit var docWarn:TextView

    private lateinit var kmCheckBtn: Button
    private lateinit var kmCheckET: EditText


    private lateinit var ovIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        //experiments have confirmed that layouts contain all their elements and are held accountable for their actions

        create = findViewById(R.id.createEntriesGroup)
        calculator = findViewById(R.id.calculatorGroup)
        overview = findViewById(R.id.carOverviewGroup)
        update = findViewById(R.id.modifyEntriesGroup)
        documents = findViewById(R.id.checkDocumentsGroup)
        parts = findViewById(R.id.checkPartsGroup)
        settings = findViewById(R.id.settingsGroup)

        partAtt = findViewById(R.id.partsAttentionTextView)
        docAtt = findViewById(R.id.docAttentionTextView)
        partWarn = findViewById(R.id.partWarningTextView)
        docWarn = findViewById(R.id.docWarningTextView)

        kmCheckBtn = findViewById(R.id.checkKmButton)
        kmCheckET = findViewById(R.id.kmCheckPartsOverview)

        kmCheckBtn.setOnClickListener { if (kmCheckET.text.isNotEmpty()) { updateOverview(kmCheckET.text.toString().toInt()) }}

        ovIV = findViewById(R.id.overview)

        updateOverview()

        settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        create.setOnClickListener{
            startActivity(Intent(this, CreateEntryActivity::class.java))
        }
        calculator.setOnClickListener{
            startActivity(Intent(this, CalculatorActivity::class.java))
        }
        update.setOnClickListener{
            val myDb = Database(this)
            val db = myDb.readableDatabase
            var parts = false
            var doc = false
            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery("SELECT * FROM ${Database.TABLE_PARTS}", null)
                parts = cursor.moveToFirst()
                cursor = db.rawQuery("SELECT * FROM ${Database.TABLE_DOCUMENTS}", null)
                doc = cursor.moveToFirst()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.close()
                cursor?.close()
            }

            if (!doc && !parts) {
                Toast.makeText(this, getString(R.string.nothing_to_modify), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ModifyEntryActivity::class.java)
            intent.putExtra("parts?", parts)
            intent.putExtra("docs?", doc)

            startActivity(intent)
        }
        documents.setOnClickListener{
            val intent = Intent(this, ItemsOverviewActivity::class.java)
            intent.putExtra("mode", "doc")
            startActivity(intent)
        }
        parts.setOnClickListener{
            val intent = Intent(this, ItemsOverviewActivity::class.java)
            intent.putExtra("mode", "part")
            startActivity(intent)
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun updateOverview(km: Int = 0){
        val myDb = Database(this)
        var db: SQLiteDatabase? = null
        var c: Cursor? = null
        val arrParts = arrayOf(ArrayList(), ArrayList<ModelItem>())
        val arrDocs = arrayOf(ArrayList(), ArrayList<ModelItem>())

        val sdf = SimpleDateFormat("yyyy/MM/dd")
        var query: String

        //check for due parts
        try {
            db = myDb.readableDatabase
            query = "SELECT ${Database.PARTS_COLUMN_NAME}, ${Database.PARTS_COLUMN_PIC}, ${Database.PARTS_COLUMN_KM}, ${Database.PARTS_COLUMN_TIME}, ${Database.PARTS_COLUMN_ID} " +
                    "FROM ${Database.TABLE_PARTS}"
            c = db.rawQuery(query, null)

            if (c.moveToFirst()) {
                var dist = getString(R.string.km)
                var mult = 1.0
                if (!myDb.getIfMetric(myDb.readableDatabase)){
                    dist = getString(R.string.mi)
                    mult = Calc.KM_MI_RATIO
                }
                do{
                    val months = c.getInt(4)
                    val tempDb = myDb.readableDatabase
                    val cursor = tempDb.rawQuery("SELECT ${Database.FIX_COLUMN_PART_ID}, ${Database.FIX_COLUMN_DATE}, ${Database.FIX_COLUMN_KM} FROM ${Database.TABLE_FIX} WHERE ${Database.FIX_COLUMN_PART_ID} = ${c.getInt(1)}", null)
                    if (cursor.moveToFirst()){
                        val modelDate = sdf.parse(sdf.format(cursor.getString(1)))
                        val dueDate = sdf.parse(getMonthsBefore(months).toString())
                        if ((c.getInt(3) != 0 && dueDate!! < modelDate) || (km != 0 && km > cursor.getInt(2) + c.getInt(2))){
                            arrParts[0].add(ModelItem(
                                    c.getString(1),
                                    c.getString(0),
                                    getString(R.string.change_every) + "($dist)",
                                    getString(R.string.change_every) + "(${getString(R.string.months)})",
                                    (c.getInt(2) / mult).toString(),
                                    c.getString(3),
                                    getString(R.string.part_warning),
                                    c.getInt(4)))
                        } else if ((c.getInt(3) != 0 && sdf.parse(getMonthsBefore(months - 1).toString())!! > modelDate) || (km != 0 && km - 500 > cursor.getInt(2) + c.getInt(2))){
                            arrParts[0].add(ModelItem(
                                c.getString(1),
                                c.getString(0),
                                getString(R.string.change_every) + "($dist)",
                                getString(R.string.change_every) + "(${getString(R.string.months)})",
                                (c.getInt(2) / mult).toString(),
                                c.getString(3),
                                getString(R.string.part_attention),
                                c.getInt(4)))
                        }
                    }
                    cursor.close()
                } while (c.moveToNext())
            }
        }catch (e:SQLException){
            e.printStackTrace()
        }finally {
            c?.close()
            db?.close()
        }


        //check for expired documents
        try {
            db = myDb.readableDatabase
            query = "SELECT ${Database.DOCUMENTS_COLUMN_NAME}, ${Database.DOCUMENTS_COLUMN_PIC}, ${Database.DOCUMENTS_COLUMN_TODATE}, ${Database.DOCUMENTS_COLUMN_ID} " +
                    "FROM ${Database.TABLE_DOCUMENTS} ORDER BY ${Database.DOCUMENTS_COLUMN_TODATE} ASC"

            c = db.rawQuery(query, null)

            if (c.moveToFirst()){
                val today = sdf.parse(sdf.format(Date()))
                val compareDate = getDaysAgo(-14)

                var added: Boolean
                do {
                    val modelDate = sdf.parse(c.getString(2))
                    added = false
                    if (modelDate!! < today){
                        arrDocs[1].add(ModelItem(
                            picturePath = c.getString(1),
                            name = c.getString(0),
                            changeField1Text = getString(R.string.expires_on),
                            changeField2Text = "",
                            valueField1Text = c.getString(2),
                            valueField2Text = "",
                            message = getString(R.string.document_warning),
                            id = c.getInt(3)))
                        added = true
                    } else if (modelDate < compareDate){
                        arrDocs[0].add(ModelItem(
                            picturePath = c.getString(1),
                            name = c.getString(0),
                            changeField1Text = getString(R.string.expires_on),
                            changeField2Text = "",
                            valueField1Text = c.getString(2),
                            valueField2Text = "",
                            message = getString(R.string.document_attention),
                            id = c.getInt(3)))
                        added = true
                    }
                } while (c.moveToNext() && added)

            }
        }catch (e:SQLException){
            e.printStackTrace()
        }finally {
            c?.close()
            db?.close()
        }

        ovIV.setImageResource(R.drawable.noworries)
        if (arrParts[0].size > 0 || arrDocs[0].size > 0){
            ovIV.setImageResource(R.drawable.attention)
        }
        if (arrParts[1].size > 0 || arrDocs[1].size > 0){
            ovIV.setImageResource(R.drawable.warning)
        }

        if (arrParts[0].size > 0){
            val s = "${arrParts[0].size}" + getString(R.string.parts_need_attention)
            partAtt.text = s
            partAtt.isInvisible = false
        } else partAtt.isInvisible = true
        if (arrParts[1].size > 0){
            val s = "${arrParts[1].size}" + getString(R.string.parts_critical)
            partWarn.text = s
            partWarn.isInvisible = false
        } else partWarn.isInvisible = true
        if (arrDocs[0].size > 0){
            val s = "${arrDocs[0].size}" + getString(R.string.docs_need_attention)
            docAtt.text = s
            docAtt.isInvisible = false
        } else docAtt.isInvisible = true
        if (arrDocs[1].size > 0){
            val s = "${arrDocs[1].size}" + getString(R.string.docs_expired)
            docWarn.text = s
            docWarn.isInvisible = false
        } else docWarn.isInvisible = true

        if (arrDocs[0].size > 0 || arrDocs[1].size > 0 || arrParts[0].size > 0 || arrParts[1].size > 0){
            overview.setOnClickListener {
                val intent = Intent(this, OverviewActivity::class.java)
                val arr = ArrayList<ModelItem>()
                arr.apply {
                    addAll(arrParts[0])
                    addAll(arrParts[1])
                    addAll(arrDocs[0])
                    addAll(arrDocs[1])
                }
                intent.removeExtra("data")
                intent.putExtra("data", arr)
                startActivity(intent)
            }
        } else {
            overview.setOnClickListener { Toast.makeText(this, getString(R.string.message_ok), Toast.LENGTH_SHORT).show() }
        }

    }

    override fun onResume() {
        super.onResume()
        updateOverview()
    }

    companion object{
        fun getDaysAgo(daysAgo: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

            return calendar.time
        }
        fun getMonthsBefore(months: Int): Date{
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -months)

            return calendar.time
        }
    }


}