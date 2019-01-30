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
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_bal.view.*
import kotlinx.android.synthetic.main.list_layout.view.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.HashSet

class BalFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null
    lateinit var protocols:MutableSet<String>
//    override fun onResume() {
//        printProtocols()
//        sharedPreferences.edit().remove("protocols").apply()
//        super.onResume()
//    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bal, container, false)
        sharedPreferences = activity!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)
        session = Session(sharedPreferences).getAll()
        user = User(session["login"]!!,session["password"]!!)
        protocols = HashSet<String>(sharedPreferences.getStringSet("protocols", HashSet<String>()))

        Log.d("-----sessss", sharedPreferences.all.toString() + " - ")
        val login = user?.login
        val password = user?.password
        val url = "http://test.azweb.dk/api/test" //"https://cerime.mia.gov.az/Dispatcher"
        val params:MutableMap<String,String?> = HashMap()
        params["next.page"] = "RegisteredLogin?lang=az"
        params["uname"] = login
        params["psw"] = password

        val headers:MutableMap<String,String>? = HashMap()
        headers!!["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
        headers["Content-Type"] = "application/x-www-form-urlencoded"


        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                notification()
                Query(context!!).post(url,params,headers,object:ResponseCallBack{
                    override fun onSuccess(response: String?) {
                        val res = response?.string()
                        val document = Jsoup.parse(res)
                        val bals = document.select("#newspaper-b tbody tr")

                        if(!protocols.containsAll(bals.eachText())) {
                            for (bal in bals) {
                                val bprotokol = bal.allElements[5].text()
                                if (!protocols.contains(bprotokol)) {
                                    protocols.add(bprotokol)

                                }
                            }
                            val editor = sharedPreferences.edit()
                            editor.putStringSet("protocols", protocols)
                            editor.apply()
                            Log.d("------old protocols",protocols.toString())




                        }

                        if(bals.size>0){
                            val adapter = BalAdapter(bals,activity!!)
                            view.list.adapter = adapter
                        }else view.emptyText.visibility = View.VISIBLE

                        view.loader.visibility = View.GONE
                    }

                })
            }
        }, 0, 5000)


        return view
    }

    fun printProtocols(){
        val protocolsAll = sharedPreferences.getStringSet("protocols", hashSetOf())
        Log.d("------afterAll",protocolsAll.toString())
    }

    fun notification(){
        val notification = NotificationCompat.Builder(activity!!, "notify_protocol")
        val ii = Intent(activity, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(activity, 0, ii, 0)
        notification.setContentText("Yeni cərimə balınız var")
        notification.setContentIntent(pendingIntent)
        notification.setSmallIcon(R.mipmap.background)

        val mNotificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("notify_protocol", "Cərimə", NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager.createNotificationChannel(channel)
        }
        mNotificationManager.notify(0, notification.build())
    }

    fun String.string():String{
        return String(this.toByteArray(Charsets.ISO_8859_1),Charsets.UTF_8)
    }


}

class BalAdapter(val data:Elements,val context: Context):RecyclerView.Adapter<ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_layout,parent,false)


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
        val carNumber = tr[4].text()
        val bal = tr[6].text()
        val start_date = tr[7].text()
        val end_date = tr[8].text()
        val protocol = tr[5].text()

        val view = holder.itemView
        view.protocol.text = protocol
        view.start_date.text = start_date
        view.bal.text = bal

        view.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_layout,null)
            dialogView.dname.text = name
            dialogView.dsurname.text = surname
            dialogView.dfatherName.text = fatherName
            dialogView.dcarNumber.text = carNumber
            dialogView.dprotocol.text = protocol
            dialogView.dbal.text = bal
            dialogView.dstart_date.text = start_date
            dialogView.dend_date.text = end_date
            dialog.setView(dialogView)
            dialog.setCancelable(true)
            dialog.setTitle("Ətraflı məlumat")
            dialog.setNegativeButton(Html.fromHtml("<font color='#005999'>Bağla</font>")){ _, _->}
            dialog.create().show()
        }

    }

}

class ViewHolder(v:View):RecyclerView.ViewHolder(v)
