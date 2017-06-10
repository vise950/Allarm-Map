package nicola.dev.com.allarmap.utils

import android.Manifest
import android.location.Location
import android.location.LocationManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import nicola.dev.com.allarmap.retrofit.MapsGoogleApiClient
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*
import nicola.dev.com.allarmap.R
import android.app.ActivityManager
import android.support.v7.app.AlertDialog


class Utils {
    companion object {

        fun trimString(s: String): String {
            val index = s.indexOf(',')
            val lastIndex = s.lastIndexOf(',')
            if (index != lastIndex) {
                return s.substring(0, index) + s.substring(lastIndex, s.length)
            }
            return s
        }

        fun hideKeyboard(context: Context) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        fun isPermissionGranted(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        fun isMyServiceRunning(context: Context,serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
        }

        fun getNowInMls(): Long {
            return System.currentTimeMillis()
        }
    }

    object LocationHelper {

        private val INVALID_DOUBLE_VALUE = -999.0

        fun getLocationName(latitude: Double, longitude: Double, onSuccess: ((String) -> Unit)? = null, onError: (() -> Unit)? = null) {
            MapsGoogleApiClient.service.getLocationName(latitude.toString() + "," + longitude.toString())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ data ->
                        if (data?.results?.isNotEmpty() ?: false) {
                            data?.results?.get(0)?.address_components?.forEach {
                                if (it.types?.get(0) == "locality" || it.types?.get(0) == "administrative_area_level_3") {
                                    onSuccess?.invoke(it.long_name.toString())
                                }
                            }
                        }
                    }, { error ->
                        error.log("get location name error")
                        onError?.invoke()
                    })
        }

        fun getCoordinates(cityName: String, onSuccess: ((Location) -> Unit)? = null) {
            MapsGoogleApiClient.service.getCoordinates(cityName).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ data ->
                        if (data?.results?.isNotEmpty() ?: false) {
                            val location = Location(LocationManager.PASSIVE_PROVIDER)
                            location.latitude = data?.results?.get(0)?.geometry?.location?.lat ?: INVALID_DOUBLE_VALUE
                            location.longitude = data?.results?.get(0)?.geometry?.location?.lng ?: INVALID_DOUBLE_VALUE
                            onSuccess?.invoke(location)
                        }
                    }, { error ->
                        error.log("get coordinates error")
                    })
        }
    }

    object AlertHepler {

        fun dialog(context: Context, title: Int, message: Int, positiveClick: (() -> Unit)? = null, negativeClick: (() -> Unit)? = null) {
            AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.action_Ok, { dialog, which ->
                        positiveClick?.invoke()
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.action_cancel, { dialog, which ->
                        negativeClick?.invoke()
                        dialog.dismiss()
                    })
                    .show()
        }

        fun snackbar(activity: Activity, message: Int, view: View = activity.root_container,
                     duration: Int = Snackbar.LENGTH_INDEFINITE,
                     actionMessage: Int = R.string.action_Ok,
                     actionClick: (() -> Unit)? = null) {
            val snackBar = Snackbar.make(view, message, duration)
            if (duration == Snackbar.LENGTH_INDEFINITE) {
                snackBar.setAction(actionMessage, { actionClick?.invoke() })
            }
            showSnackBar(activity, snackBar)
        }

        private fun showSnackBar(activity: Activity, snackBar: Snackbar) {
            //todo repleace runOnUiThread with anko or koventant
            activity.runOnUiThread {
                snackBar.show()
            }
        }
    }
}