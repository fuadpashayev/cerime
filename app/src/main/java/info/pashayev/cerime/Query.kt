package info.pashayev.cerime

import android.content.Context
import android.content.SharedPreferences
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.widget.PopupMenu
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

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

class Menu(val context: Context,val view:ConstraintLayout):PopupMenu(context,view){

}