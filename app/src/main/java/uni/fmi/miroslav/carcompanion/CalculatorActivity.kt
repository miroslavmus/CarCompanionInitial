package uni.fmi.miroslav.carcompanion

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import uni.fmi.miroslav.carcompanion.customelements.ToggleTextView
import uni.fmi.miroslav.carcompanion.tools.Calc
import uni.fmi.miroslav.carcompanion.tools.Database
import java.sql.SQLException

@SuppressLint("UseSwitchCompatOrMaterialCode")
class CalculatorActivity : AppCompatActivity() {

    private lateinit var unitTV: ToggleTextView
    private lateinit var unitET: EditText
    private lateinit var distTV: ToggleTextView
    private lateinit var distET: EditText

    private lateinit var saveSwitch: Switch

    private lateinit var toggleTV: ToggleTextView

    private lateinit var mpgBtn: Button
    private lateinit var lkmBtn: Button

    private lateinit var resLastTV: ToggleTextView
    private lateinit var resTV: ToggleTextView
    private lateinit var resAvgTV: ToggleTextView

    private var isMetric: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        //units TV's and ET's

        unitET = findViewById(R.id.fuelCalculatorEditText)
        unitTV = ToggleTextView(
            findViewById(R.id.unitFuelCalculatorTextView),
            getString(R.string.li),
            getString(R.string.gal)
        )
        distET = findViewById(R.id.distanceCalculatorEditText)
        distTV = ToggleTextView(
            findViewById(R.id.unitDistCalculatorTextView),
            getString(R.string.km),
            getString(R.string.mi)
        )

        //save switch

        saveSwitch = findViewById(R.id.saveResultCalculatorSwitch)
        saveSwitch.setOnClickListener { saveSwitch.isActivated = !saveSwitch.isActivated }
        saveSwitch.isActivated = false

        //calculate buttons

        mpgBtn = findViewById(R.id.mpgCalculatorButton)
        lkmBtn = findViewById(R.id.kmCalculatorButton)

        //TV displays

        resTV = ToggleTextView(
            findViewById(R.id.resultCalculatorTextView)
        )
        resAvgTV = ToggleTextView(
            findViewById(R.id.avgResultCalculatorTextView)
        )
        resLastTV = ToggleTextView(
            findViewById(R.id.prevResultCalculatorTextView)
        )

        //setup units on screen and update with Database

        val db = Database(this)
        isMetric = db.getIfMetric(db.readableDatabase)
        unitTV.switchTo(isMetric)
        distTV.switchTo(isMetric)

        //setup and update mpg/lp100km Toggle TV

        toggleTV = ToggleTextView(
            findViewById(R.id.toggleCalculatorTextView),
            getString(R.string.lp100km),
            getString(R.string.mpg)
        )

        //onClick listeners for units and toggle result unit

        unitTV.tv.setOnClickListener { unitTV.toggle() }
        distTV.tv.setOnClickListener { distTV.toggle() }
        toggleTV.tv.setOnClickListener { switchFields(!toggleTV.isOn) }

        //onClick listeners for mpg and li/100km calculate buttons

        mpgBtn.setOnClickListener { calculate(mpgBtn) }
        lkmBtn.setOnClickListener { calculate(lkmBtn) }


    }

    private fun calculate(btn: Button){

        if (distET.text.isEmpty() || unitET.text.isEmpty()) { return }

        val result : Double
        var km: Double = distET.text.toString().toDouble()
        var li: Double = unitET.text.toString().toDouble()

        if (km <= 0.1 || li <= 0.1) { return }

        //convert to proper metric units :P
        if (!distTV.isOn){
            km = Calc.miToKm(km)
        }
        if (!unitTV.isOn){
            li = Calc.galToLi(li)
        }


        //calculate result
        result = Calc.getLKM(km, li)

        //fill the three text views with info

        resTV = ToggleTextView(
            resTV.tv,
            formatDouble(result),
            formatDouble(Calc.convert(result))
        )

        val cv = getData()
        val last: Double? = cv?.getAsDouble("last")
        val avg: Double? = cv?.getAsDouble("avg")

        if (last != null){
            resAvgTV =
                ToggleTextView(
                    resAvgTV.tv,
                    formatDouble(avg!!),
                    formatDouble(Calc.convert(avg))
                )
            resLastTV =
                ToggleTextView(
                    resLastTV.tv,
                    formatDouble(last),
                    formatDouble(Calc.convert(last))
                )
        }


        //this syncs the bottom TV with the button for calculation that is pressed
        //also now syncs the other TV's
        if (toggleTV.isOn.xor(btn.id == R.id.kmCalculatorButton)){
            switchFields(btn.id == R.id.kmCalculatorButton)
        }

        if (saveSwitch.isActivated) {
            save(li, km)
            saveSwitch.toggle()
        }

    }

    private fun formatDouble(num : Double): String{
        return "${((num * 100).toInt() / 100.0)}"
    }

    private fun getData(): ContentValues?{

        var database: SQLiteDatabase? = null
        try {
            val db = Database(this)
            database = db.readableDatabase
            val cv = ContentValues()

            val last = db.getLastFillup(database) ?: return null

            cv.put("last", last)
            cv.put("avg", db.getAverageFillup(database))

            return cv
        } catch (e: SQLException){
            e.printStackTrace()
        } finally {
            database?.close()
        }

        return null
    }

    private fun save(fuel: Double, km: Double){
        var db: Database? = null
        var id: Long? = null
        try {
            db = Database(this)
            id = db.insertFillUp(db.writableDatabase, fuel, km)

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db!!.close()
        }

        Toast.makeText(this, "result saved under id:$id", Toast.LENGTH_SHORT).show()
    }

    private fun switchFields(boolean: Boolean){
        toggleTV.switchTo(boolean)
        resLastTV.switchTo(boolean)
        resAvgTV.switchTo(boolean)
        resTV.switchTo(boolean)
    }


    override fun onBackPressed() {
        this.finish()
    }




}