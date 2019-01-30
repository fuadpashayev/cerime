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


class HomeActivity : AppCompatActivity(),ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("--------connection","disconnected")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("--------connection","connected")
    }

    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null


    override fun onStop() {
        val intent = Intent(this, CerimeService::class.java)
        startService(intent)
        super.onStop()
        Log.d("------onStop","onStop")
    }


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
                        sharedPreferences.edit().remove("protocols").apply()
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
