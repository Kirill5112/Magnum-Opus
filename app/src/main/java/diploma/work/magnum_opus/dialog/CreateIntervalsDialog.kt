package diploma.work.magnum_opus.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import diploma.work.magnum_opus.R
import androidx.appcompat.app.AlertDialog

class CreateIntervalsDialog : DialogFragment() {
    private lateinit var mListener: OnIntervalsCreatedListener

    interface OnIntervalsCreatedListener {
        fun onIntervalsCreated(title: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnIntervalsCreatedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_create_intervals, null)

        val editDeckName = view.findViewById<EditText>(R.id.edit_deck_name)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnOk.setOnClickListener {
            val title = editDeckName.text.toString()
            if (title.isNotEmpty()) {
                mListener.onIntervalsCreated(title)
                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        builder.setTitle(null)
        return builder.create()
    }
}