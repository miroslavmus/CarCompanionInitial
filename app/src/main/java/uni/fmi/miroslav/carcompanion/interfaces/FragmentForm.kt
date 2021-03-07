package uni.fmi.miroslav.carcompanion.interfaces

import android.content.ContentValues
import androidx.fragment.app.Fragment

interface FragmentForm {
    fun fragmentPartsData(contentValues: ContentValues){}
    fun fragmentDocumentsData(contentValues: ContentValues){}
    fun onFragmentAttach(fragment: Fragment){}
}