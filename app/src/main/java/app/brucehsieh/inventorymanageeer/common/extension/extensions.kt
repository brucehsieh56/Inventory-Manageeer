package app.brucehsieh.inventorymanageeer.common.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.ShortSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun View.LongSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}