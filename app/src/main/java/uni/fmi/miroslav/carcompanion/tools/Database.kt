package uni.fmi.miroslav.carcompanion.tools

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import uni.fmi.miroslav.carcompanion.R
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date

class Database(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(CREATE_TABLE_PARTS)
        db.execSQL(CREATE_TABLE_FIX)
        db.execSQL(CREATE_TABLE_FILLUP)
        db.execSQL(CREATE_TABLE_DOCUMENTS)


        db.execSQL(CREATE_TABLE_SETTINGS)
        val cv: ContentValues = ContentValues().apply {
            put(
                SETTINGS_COLUMN_CARNAME,
                R.string.default_car_name
            )
            put(SETTINGS_COLUMN_METRIC, 1)
            put(SETTINGS_COLUMN_TUTORIAL, 1)
        }
        db.insert(TABLE_SETTINGS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FIX")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FILLUP")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DOCUMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    fun onDataWipe(db: SQLiteDatabase, complete: Boolean = false){
        db.execSQL("DELETE FROM $TABLE_FIX")
        db.execSQL("DELETE FROM $TABLE_FILLUP")
        if (complete) {
            db.execSQL("DELETE FROM $TABLE_DOCUMENTS")
            db.execSQL("DELETE FROM $TABLE_PARTS")
        }
    }

    fun getAverageFillup(db: SQLiteDatabase): Double?{
        val cursor: Cursor = db.rawQuery("SELECT $FILLUP_COLUMN_KM, $FILLUP_COLUMN_FUEL FROM $TABLE_FILLUP", null)
        var sumKm = 0.0
        var sumLi = 0.0
        if (!cursor.moveToFirst()) return null
        do{
            sumKm += cursor.getDouble(0)
            sumLi += cursor.getDouble(1)
        }while (cursor.moveToNext())
        cursor.close()
        return Calc.getLKM(sumKm, sumLi)
    }
    fun getLastFillup(db: SQLiteDatabase): Double?{
        val cursor: Cursor = db.rawQuery("SELECT $FILLUP_COLUMN_KM, $FILLUP_COLUMN_FUEL, $FILLUP_COLUMN_DATE FROM $TABLE_FILLUP ORDER BY $FILLUP_COLUMN_DATE DESC",null)
        if (!cursor.moveToFirst()) return null
        val res = Calc.getLKM(cursor.getDouble(0), cursor.getDouble(1))
        cursor.close()
        return res
    }

    //INSERTS
    @SuppressLint("SimpleDateFormat")
    fun insertFillUp(db: SQLiteDatabase, fuel: Double, km: Double): Long{
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
        val strDate = sdf.format(Date())
        val cv: ContentValues = ContentValues().apply {
            put(FILLUP_COLUMN_DATE, strDate)
            put(FILLUP_COLUMN_FUEL, fuel)
            put(FILLUP_COLUMN_KM, km)
        }
        return db.insert(TABLE_FILLUP, null, cv)
    }


    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    fun getIfMetric(db: SQLiteDatabase): Boolean {
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery("SELECT $SETTINGS_COLUMN_METRIC FROM $TABLE_SETTINGS", null)
            cursor.moveToFirst()
            return cursor.getInt(0) == 1
        }catch(e: SQLException){
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return true
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 11
        const val DATABASE_NAME = "Database.db"

        // commands:

        //CREATE TABLE PARTS
        const val TABLE_PARTS = "parts"
        const val PARTS_COLUMN_ID = "id"
        const val PARTS_COLUMN_PIC = "pic"
        const val PARTS_COLUMN_NAME = "name"
        const val PARTS_COLUMN_KM = "km"
        const val PARTS_COLUMN_TIME = "time"

        private const val CREATE_TABLE_PARTS = "CREATE TABLE $TABLE_PARTS ( " +
            "$PARTS_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PARTS_COLUMN_PIC VARCHAR(20) NOT NULL, " +
                "$PARTS_COLUMN_NAME NVARCHAR(20) UNIQUE, " +
                "$PARTS_COLUMN_KM INTEGER, " +
                "$PARTS_COLUMN_TIME INTEGER)"

        //CREATE TABLE FIX
        const val TABLE_FIX = "fix"
        const val FIX_COLUMN_PART_ID = "part_id"
        const val FIX_COLUMN_KM = "km"
        const val FIX_COLUMN_DATE = "date"
        const val FIX_COLUMN_INFO = "info"

        private const val CREATE_TABLE_FIX = "CREATE TABLE $TABLE_FIX ( " +
                "$FIX_COLUMN_PART_ID INTEGER NOT NULL, " +
                "$FIX_COLUMN_KM INTEGER NOT NULL, " +
                "$FIX_COLUMN_DATE DATE NOT NULL, " +
                "$FIX_COLUMN_INFO NVARCHAR(300), " +
                "FOREIGN KEY($FIX_COLUMN_PART_ID) REFERENCES $TABLE_PARTS($PARTS_COLUMN_ID) ON UPDATE CASCADE)"

        //CREATE TABLE FILLUP
        const val TABLE_FILLUP = "fillup"
        const val FILLUP_COLUMN_ID = "id"
        const val FILLUP_COLUMN_DATE = "date"
        const val FILLUP_COLUMN_KM = "km"
        const val FILLUP_COLUMN_FUEL = "fuel"

        private const val CREATE_TABLE_FILLUP = "CREATE TABLE $TABLE_FILLUP ( " +
                "$FILLUP_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$FILLUP_COLUMN_DATE DATETIME NOT NULL, " +
                "$FILLUP_COLUMN_KM DOUBLE NOT NULL, " +
                "$FILLUP_COLUMN_FUEL DOUBLE NOT NULL )"


        //CREATE SETTINGS TABLE
        const val TABLE_SETTINGS = "settings"
        const val SETTINGS_COLUMN_CARNAME = "carName"
        const val SETTINGS_COLUMN_METRIC = "isMetric"
        const val SETTINGS_COLUMN_TUTORIAL = "doTutorial"

        const val CREATE_TABLE_SETTINGS = "CREATE TABLE $TABLE_SETTINGS ( " +
                "$SETTINGS_COLUMN_CARNAME VARCHAR(20) NOT NULL, " +
                "$SETTINGS_COLUMN_METRIC INTEGER NOT NULL," +
                "$SETTINGS_COLUMN_TUTORIAL INTEGER NOT NULL )"



        //CREATE TABLE DOCUMENTS
        const val TABLE_DOCUMENTS = "documents"
        const val DOCUMENTS_COLUMN_ID = "id"
        const val DOCUMENTS_COLUMN_PIC = "pic"
        const val DOCUMENTS_COLUMN_NAME = "name"
        const val DOCUMENTS_COLUMN_TODATE = "toDate"

        const val CREATE_TABLE_DOCUMENTS = "CREATE TABLE $TABLE_DOCUMENTS ( " +
                "$DOCUMENTS_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$DOCUMENTS_COLUMN_PIC VARCHAR(20) NOT NULL, " +
                "$DOCUMENTS_COLUMN_NAME VARCHAR(20) NOT NULL UNIQUE, " +
                "$DOCUMENTS_COLUMN_TODATE DATE NOT NULL )"

    }
}