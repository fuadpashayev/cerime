package info.pashayev.cerime


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_register.view.*
import org.jsoup.Jsoup


class RegisterFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    val Preference = "session"
    var user: User? = null
    lateinit var editor: SharedPreferences.Editor
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        sharedPreferences = context!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)
        view.signUp.setOnClickListener {
            val license = view.license.text.toString()
            val birth = view.birth.text.toString()
            val valid = view.valid.text.toString()
            val name = view.name.text.toString()
            val surname = view.surname.text.toString()
            val login = view.login.text.toString()
            val password = view.password.text.toString()
            val password_confirmation = view.password_confirmation.text.toString()
            val email = view.email.text.toString()

            if(!check("license",license)){
                Toast.makeText(activity!!,"Sürücülük vəsiqəsinin nömrəsini düzgün daxil edin! məs.: AA123456",Toast.LENGTH_LONG).show()
            }else if(!check("birth",birth)){
                Toast.makeText(activity!!,"Doğum tarixini düzgün daxil edin! məs.: 07.04.1975",Toast.LENGTH_LONG).show()
            }else if(!check("valid",valid)){
                Toast.makeText(activity!!,"Sürücülük vəsiqəsinin etibarlı olduğu son tarixi düzgün daxil edin! məs.: 07.04.1975",Toast.LENGTH_LONG).show()
            }else if(!check("name",name)){
                Toast.makeText(activity!!,"Adınızı düzgün daxil edin!",Toast.LENGTH_LONG).show()
            }else if(!check("surname",surname)){
                Toast.makeText(activity!!,"Soyadınızı düzgün daxil edin!",Toast.LENGTH_LONG).show()
            }else if(!check("login",login)){
                Toast.makeText(activity!!,"İstifadəçi adınızı düzgün daxil edin!",Toast.LENGTH_LONG).show()
            }else if(!check("password",password)){
                Toast.makeText(activity!!,"Şifrənizi düzgün daxil edin! min. 6 simvol,kiçik və böyük hərf mütləqdir",Toast.LENGTH_LONG).show()
            }else if(!check("password_confirmation",password_confirmation,password)){
                Toast.makeText(activity!!,"Təkrar şifrənizi düzgün daxil edin!",Toast.LENGTH_LONG).show()
            }else if(!check("email",email)){
                Toast.makeText(activity!!,"E-poçt ünvanınızı düzgün daxil edin!",Toast.LENGTH_LONG).show()
            }else{
                val births = birth.split(".")
                val valids = valid.split(".")
                val url = "https://cerime.mia.gov.az/Dispatcher"
                val params: MutableMap<String, String?> = HashMap()
                params["next.page"] = "Registration"
                params["vesiqeno"] = license
                params["bday"] = births[0].replace(Regex("0(\\d)"),"$1")
                params["bmonth"] = births[1].replace(Regex("0(\\d)"),"$1")
                params["byear"] = births[2]
                params["etDay"] = valids[0].replace(Regex("0(\\d)"),"$1")
                params["etMonth"] = valids[1].replace(Regex("0(\\d)"),"$1")
                params["etYear"] = valids[2]
                params["name"] = name
                params["surname"] = surname
                params["uname"] = login
                params["psw1"] = password
                params["psw2"] = password_confirmation
                params["email"] = email
                params["ajx"] = "1"
                params["ins"] = "1"


                Query(context!!).post(url,params,responseCallBack = object:ResponseCallBack{
                    override fun onSuccess(response: String?) {
                        val res = response?.string()
                        val document = Jsoup.parse(res)
                        val error = document.getElementsByClass("error")
                        val success = document.getElementsByClass("success")

                        if (error.size > 0) {
                            Toast.makeText(activity, error.text(), Toast.LENGTH_SHORT).show()
                        } else if(success.size>0){
                            Toast.makeText(activity!!,success.text(),Toast.LENGTH_LONG).show()
                            user = User(login, password)
                            editor = sharedPreferences.edit()
                            editor.putString("login", login)
                            editor.putString("password", password)
                            editor.apply()
                            val intent = Intent(activity, HomeActivity::class.java)
                            startActivity(intent)
                        }
                    }

                })

            }


        }
        return view
    }



    fun check(type:String,value:String,secondValue:String?=null):Boolean {
        return when(type){
            "license"-> value.matches(Regex("(([A-Z]){2}([0-9]){6})"))
            "birth"-> value.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))
            "valid"-> value.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))
            "name"-> value!=""
            "surname"-> value!=""
            "login"-> value.length>5
            "password"-> value.matches(Regex("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})"))
            "password_confirmation"-> value==secondValue
            "email"-> value.matches(Regex("^[a-z0-9_\\+-]+(\\.[a-z0-9_\\+-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.([a-z]{2,4})"))
            else-> false
        }
    }

    fun String.string(): String {
        return String(this.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
    }

}
