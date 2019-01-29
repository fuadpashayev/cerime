package info.pashayev.cerime

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_option.view.*


class OptionFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedPreferences = context!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)
        session = Session(sharedPreferences).getAll()
        user = User(session["login"]!!,session["password"]!!)
        val view = inflater.inflate(R.layout.fragment_option, container, false)
        view.name.text = user?.login
        view.exit.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Hesabdan çıxış")
            dialog.setMessage("Çıxış etməyə əminsiniz?")
            dialog.setCancelable(true)
            dialog.setNegativeButton(Html.fromHtml("<font color='#ce4f56'>Xeyr</font>")){ _, _->}
            dialog.setPositiveButton(Html.fromHtml("<font color='#3F51B5'>Çıxış et</font>")){_,_->
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                activity?.finish()
            }
            dialog.create().show()
        }
        return view
    }


}
