package info.pashayev.cerime

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.support.constraint.ConstraintLayout
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.PopupMenu
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*


class Query(val context: Context){
    fun get(url:String,headers:MutableMap<String,String?>?=null,responseCallBack: ResponseCallBack){
        val queue = Volley.newRequestQueue(context)
        queue.add(object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    responseCallBack.onSuccess(response)
                },
                Response.ErrorListener {
                }
        ) {
            override fun getHeaders(): MutableMap<String, String?>? {
                var inHeader:MutableMap<String,String?> = HashMap()
                if(headers!=null)
                    inHeader = headers
                else
                    inHeader["Accept"] = "application/json; charset=utf-8"
                return inHeader
            }
        })
    }

    fun post(url: String,params:MutableMap<String,String?>?,headers:MutableMap<String,String>?=null,responseCallBack: ResponseCallBack){
        val queue = Volley.newRequestQueue(context)
        queue.add(object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    responseCallBack.onSuccess(response)
                },
                Response.ErrorListener {
                }
        ) {
            override fun getHeaders(): MutableMap<String, String>? {
                var inHeader:MutableMap<String,String> = HashMap()
                if(headers!=null)
                    inHeader = headers
                else {
                    inHeader["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
                    inHeader["Content-Type"] = "application/x-www-form-urlencoded"
                }
                return inHeader
            }

            override fun getParams(): MutableMap<String, String?>? {
                return params
            }

        })
    }
}

interface ResponseCallBack {
    fun onSuccess(response: String?)
//    fun onError(error: VolleyError)
}

class User(login:Any,password:Any){
    var login:String? = null
    var password:String? = null
    init{
        this.login = login.toString()
        this.password = password.toString()
    }
}


class Session(val session:SharedPreferences){
    fun get(key:String):String{
        return session.getString(key,"")
    }
    fun getAll():MutableMap<String,*>{
        return session.all
    }

    fun addStringSet(key:String,data:MutableSet<String>){
        val edit = session.edit()
        edit.putStringSet(key,data)
        edit.apply()
    }


}

class CerimeService: Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    override fun onStartCommand(intent:Intent, flags:Int, startId:Int):Int {
        return START_STICKY
    }
    private fun startForeground() {
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
//        val notification = NotificationCompat.Builder(this, "notify_protocol")
//
//        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel("notify_protocol", "Cərimə", NotificationManager.IMPORTANCE_DEFAULT)
//            mNotificationManager.createNotificationChannel(channel)
//        }
//        startForeground(NOTIF_ID, notification
//                .setOngoing(true)
//                .setSmallIcon(R.drawable.ic_person)
//                .setContentTitle(getString(R.string.app_name))
//                .setContentText("Service is running background")
//                .setContentIntent(pendingIntent)
//                .setPriority(Notification.PRIORITY_MAX)
//                .build())
    }
    companion object {
        private val NOTIF_ID = 1
        private val NOTIF_CHANNEL_ID = "notify_cerime_service"
    }
}

