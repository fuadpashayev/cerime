package info.pashayev.cerime

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    val Preference = "session"
    var user: User? = null
    lateinit var manager: FragmentManager
    lateinit var editor: SharedPreferences.Editor
    var openedFragment = false
    var not_type = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        manager = supportFragmentManager

        if(intent.extras!=null) {
            not_type = intent.extras.getInt("not_type", 0)
        }
        val session = Session(sharedPreferences).getAll()
        if (session["login"] != null) {
            user = User(login, password)
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            intent.putExtra("not_type",not_type)
            startActivity(intent)
        }

        signIn.setOnClickListener {
            loader.visibility = View.VISIBLE
            val login = login.text.toString()
            val password = password.text.toString()
            val url = "https://cerime.mia.gov.az/Dispatcher"
            val params: MutableMap<String, String?> = HashMap()
            params["next.page"] = "RegisteredLogin?lang=az"
            params["uname"] = login
            params["psw"] = password

            Query(this).post(url, params, responseCallBack =  object : ResponseCallBack {
                override fun onSuccess(response: String?) {
                    val res = response?.string()
                    val document = Jsoup.parse(res)
                    val error = document.getElementsByClass("error").size

                    if (error > 0) {
                        Toast.makeText(this@MainActivity, "İstifadəçi adı və ya şifrə yalnışdır", Toast.LENGTH_SHORT).show()
                    } else {
                        user = User(login, password)
                        editor = sharedPreferences.edit()
                        editor.putString("login", login)
                        editor.putString("password", password)
                        editor.apply()
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        loader.visibility = View.GONE
                    }


                }

            })
        }

        signUp.setOnClickListener {
            RegisterFragment().start()
            openedFragment = true
        }

    }


    fun String.string(): String {
        return String(this.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
    }

    fun Fragment.start() {
        val transaction = manager.beginTransaction()
        val fragment = this
        var currentTag = manager.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag, "$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag, "$1")
        if (newTag != currentTag) {
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onBackPressed() {

        if(openedFragment) {
            super.onBackPressed()
            openedFragment = false
        }else
            return
    }
}
