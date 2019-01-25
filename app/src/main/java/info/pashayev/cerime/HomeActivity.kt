package info.pashayev.cerime

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import org.jsoup.Jsoup

class HomeActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        session = Session(sharedPreferences).getAll()
        user = User(session["login"]!!,session["password"]!!)

        val adapter = FragmentPager(this, supportFragmentManager)
        viewPager.adapter = adapter
        val tabLayout = sliding_tabs as TabLayout
        tabLayout.setupWithViewPager(viewPager)

        welcomeText.text = Html.fromHtml("Xoş gəldiniz, <font color=\"#00529B\">${user?.login}</font>")
        val login = user?.login
        val password = user?.password
        val url = "https://cerime.mia.gov.az/Dispatcher"
        val params:MutableMap<String,String?> = HashMap()
        params["next.page"] = "RegisteredLogin?lang=az"
        params["uname"] = login
        params["psw"] = password

        val headers:MutableMap<String,String>? = HashMap()
        headers!!["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
        headers["Content-Type"] = "application/x-www-form-urlencoded"
        Query(this).post(url,params,headers,object :ResponseCallBack{
            override fun onSuccess(response: String?) {
                val res = response?.string()
                val document = Jsoup.parse(res)
                val infos = document.getElementsByClass("info")
                val cerimebal = document.getElementById("newspaper-b")
            }

        })


        exit.setOnClickListener {
           val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Çıxış etməkdə əminsiniz?")
            dialog.setCancelable(true)
            dialog.setNegativeButton(Html.fromHtml("<font color='#ce4f56'>Xeyr</font>")){_,_->}
            dialog.setPositiveButton(Html.fromHtml("<font color='#3F51B5'>Çıxış et</font>")){_,_->
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                finish()
            }
            dialog.create().show()
        }
    }
    fun String.string():String{
        return String(this.toByteArray(Charsets.ISO_8859_1),Charsets.UTF_8)
    }

    override fun onBackPressed() {
        return
    }
}
