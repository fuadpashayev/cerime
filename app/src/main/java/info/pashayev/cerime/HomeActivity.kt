package info.pashayev.cerime

import android.content.*
import android.content.res.ColorStateList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import org.jsoup.Jsoup
import android.support.v7.view.menu.MenuPopupHelper
import java.lang.reflect.AccessibleObject.setAccessible
import java.lang.reflect.AccessibleObject.setAccessible
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.content.Intent
import android.R.string.cancel
import android.content.Context.NOTIFICATION_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.app.NotificationManager
import android.os.Build
import android.support.annotation.RequiresApi


class HomeActivity : AppCompatActivity() {


    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sIntent = Intent(this, CerimeService::class.java)
        startService(sIntent)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        session = Session(sharedPreferences).getAll()
        user = User(session["login"]!!,session["password"]!!)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0)
        notificationManager.deleteNotificationChannel("notify_protocol")
        notificationManager.deleteNotificationChannel("notify_cprotocol")

        var notificationRefer = 0
        if(intent.extras!=null)
            notificationRefer = intent.extras.getInt("not_type", 0)


        val adapter = FragmentPager(this, supportFragmentManager)
        viewPager.adapter = adapter
        val tabLayout = sliding_tabs as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(notificationRefer)?.select()
//        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_more)
//        tabLayout.tabIconTint = ColorStateList.valueOf(resources.getColor(R.color.white))
//        tabLayout.setWrapContent(2)

        menu.setOnClickListener {
            val popup = PopupMenu(this,menu)
            popup.menuInflater.inflate(R.menu.popup_menu,popup.menu)

            val welcome = popup.menu.getItem(0)
            welcome.title = user?.login
            popup.setOnMenuItemClickListener {item->
                val id = item.titleCondensed
                when(id){
                    "exit"->{
                        val dialog = AlertDialog.Builder(this)
                        dialog.setTitle("Hesabdan çıxış")
                        dialog.setMessage("Çıxış etməyə əminsiniz?")
                        dialog.setCancelable(true)
                        dialog.setNegativeButton("Xeyr"){_,_->}
                        dialog.setPositiveButton("Çıxış et"){_,_->
                            val editor = sharedPreferences.edit()
                            editor.remove("login")
                            editor.remove("password")
                            editor.apply()
                            finish()
                        }
                        dialog.create().show()
                    }

                    "clearBal"->{
                        val editor = sharedPreferences.edit()
                        editor.remove("protocols")
                        editor.remove("cprotocols")
                        editor.apply()
                        Toast.makeText(this,"Yaddaş təmizləndi",Toast.LENGTH_LONG).show()
                    }
                }
                return@setOnMenuItemClickListener true
            }

            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popup)
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopup, true)

            popup.show()

        }

    }

    fun TabLayout.setWrapContent(tabPosition: Int) {
        val layout = (this.getChildAt(0) as LinearLayout).getChildAt(tabPosition) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0f
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layout.layoutParams = layoutParams
    }

    fun String.string():String{
        return String(this.toByteArray(Charsets.ISO_8859_1),Charsets.UTF_8)
    }

    fun ConstraintLayout.toggle(){
        val visibility = this.visibility
        when(visibility){
            View.VISIBLE->this.visibility = View.GONE
            View.GONE->this.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        return
    }
}
