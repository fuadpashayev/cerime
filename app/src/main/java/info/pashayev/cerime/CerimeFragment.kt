package info.pashayev.cerime


import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_bal.view.*
import kotlinx.android.synthetic.main.list_clayout.view.*
import kotlinx.android.synthetic.main.dialog_clayout.view.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.HashSet
import android.media.RingtoneManager
import android.media.Ringtone


class CerimeFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null
    lateinit var cprotocols:MutableSet<String>
    var timer:Timer? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cerime, container, false)
        sharedPreferences = activity!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)
        session = Session(sharedPreferences).getAll()
        user = User(session["login"]!!,session["password"]!!)
        cprotocols = HashSet(sharedPreferences.getStringSet("cprotocols", HashSet<String>()))

        val login = user?.login
        val password = user?.password
        val url = "https://cerime.mia.gov.az/Dispatcher" //"http://test.azweb.dk/api/test" //
        val params:MutableMap<String,String?> = HashMap()
        params["next.page"] = "RegisteredLogin?lang=az"
        params["uname"] = login
        params["psw"] = password

        val headers:MutableMap<String,String>? = HashMap()
        headers!!["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
        headers["Content-Type"] = "application/x-www-form-urlencoded"

        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Query(context!!).post(url,params,headers,object:ResponseCallBack{
                    override fun onSuccess(response: String?) {
                        val res = response?.string()
                        val document = Jsoup.parse(res)
                        val newspaperAll = document.select("#newspaper-b").last()
                        var cerimes = document.select("#newspaper-b")
                        if(newspaperAll!=null){
                            cerimes = document.select("#newspaper-b").last().select("tbody tr")
                            val adapter = CerimeAdapter(cerimes,activity!!)
                            view.list.adapter = adapter
                        }else view.emptyText.visibility = View.VISIBLE


                        if(!cprotocols.containsAll(cerimes.eachText())) {
                            for (cerime in cerimes) {
                                val bprotokol = cerime.allElements[4].text()
                                if (!cprotocols.contains(bprotokol)) {
                                    cprotocols.add(bprotokol)
                                    notification()
                                }
                            }
                            val editor = sharedPreferences.edit()
                            editor.putStringSet("cprotocols", cprotocols)
                            editor.apply()

                        }



                        view.loader.visibility = View.GONE
                    }

                })
            }
        }, 0, 15000)




        return view
    }

    override fun onStop() {
        super.onStop()
        if(timer!=null)
            timer!!.cancel()

    }

    fun notification(){
        val notification = NotificationCompat.Builder(activity!!, "notify_cprotocol")
        val ii = Intent(activity, MainActivity::class.java)
        ii.putExtra("not_type",1)
        ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(activity, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.setContentText("Yeni cəriməniz var")
        notification.setContentIntent(pendingIntent)
        notification.setSmallIcon(R.drawable.ic_info)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(context, alarmSound)
        r.play()

        val mNotificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("notify_cprotocol", "Cərimə", NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager.createNotificationChannel(channel)
        }
        mNotificationManager.notify(0, notification.build())
    }

    fun String.string():String{
        return String(this.toByteArray(Charsets.ISO_8859_1),Charsets.UTF_8)
    }


}

class CerimeAdapter(val data:Elements,val context: Context):RecyclerView.Adapter<ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_clayout,parent,false)


        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size-1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tr = data[position].allElements
        val name = tr[2].text()
        val surname = tr[1].text()
        val fatherName = tr[3].text()
        val money = tr[5].text()+" AZN"
        val start_date = tr[6].text()
        val protocol = tr[4].text()

        val view = holder.itemView
        view.protocol.text = protocol
        view.start_date.text = start_date
        view.money.text = money

        view.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_clayout,null)
            dialogView.dname.text = name
            dialogView.dsurname.text = surname
            dialogView.dfatherName.text = fatherName
            dialogView.dprotocol.text = protocol
            dialogView.dmoney.text = money
            dialogView.dstart_date.text = start_date
            dialog.setView(dialogView)
            dialog.setCancelable(true)
            dialog.setTitle("Ətraflı məlumat")
            dialog.setNegativeButton(Html.fromHtml("<font color='#005999'>Bağla</font>")){ _, _->}
            dialog.create().show()
        }

    }

}

