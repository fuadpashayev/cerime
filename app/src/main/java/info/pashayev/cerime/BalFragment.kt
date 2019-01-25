package info.pashayev.cerime


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_bal.view.*
import kotlinx.android.synthetic.main.list_layout.view.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class BalFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var session:MutableMap<String,*>
    val Preference = "session"
    var user:User?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bal, container, false)
        sharedPreferences = context!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)
        session = Session(sharedPreferences).getAll()
        user = User(session["login"]!!,session["password"]!!)
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
        Query(context!!).post(url,params,headers,object :ResponseCallBack{
            override fun onSuccess(response: String?) {
                val res = response?.string()
                val document = Jsoup.parse(res)
                val infos = document.getElementsByClass("info")
                val bals = document.select("#newspaper-b tbody tr")
                Log.d("-----bals",bals.size.toString())
                if(bals.size>0){
                    val adapter = BalAdapter(bals)
                    view.list.adapter = adapter
                }else view.emptyText.visibility = View.VISIBLE

                view.loader.visibility = View.GONE
            }

        })

        return view
    }

    fun String.string():String{
        return String(this.toByteArray(Charsets.ISO_8859_1),Charsets.UTF_8)
    }


}

class BalAdapter(val data:Elements):RecyclerView.Adapter<ViewHolder>(){
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
            Log.d("-----tr",tr.toString())
            val protocol = tr[5].text()
            val start_date = tr[7].text()
            val bal = tr[6].text()

            val view = holder.itemView
            view.protocol.text = protocol
            view.start_date.text = start_date
            view.bal.text = bal


    }

}

class ViewHolder(v:View):RecyclerView.ViewHolder(v)
