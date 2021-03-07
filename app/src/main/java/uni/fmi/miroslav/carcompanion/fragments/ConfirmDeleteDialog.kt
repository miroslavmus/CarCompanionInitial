package uni.fmi.miroslav.carcompanion.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmDeleteDialog(
    private val message: String,
    private val yes: String,
    private val cancel: String
) : DialogFragment() {

    private lateinit var dialogListener: DialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        dialogListener = context as DialogListener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setPositiveButton(yes
                ) { dialog, id ->
                    dialogListener.pressYes(tag)
                }
                .setNegativeButton(cancel
                ) { dialog, id ->
                    dialogListener.pressNo(tag)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }



    interface DialogListener{
        fun pressYes(tag: String?) {}
        fun pressNo(tag: String?) {}
    }
}
