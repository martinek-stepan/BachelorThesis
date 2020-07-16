package martinek.segasesu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import martinek.segasesu.Utils.ACCESS_CODE_SP
import martinek.segasesu.Utils.SEGASESU_SP
import martinek.segasesu.Utils.retrofitApi
import martinek.segasesu.flowers.FlowersGalleryActivity
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity()
{
    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accessCodeET.requestFocus()
        sharedPref = getSharedPreferences(SEGASESU_SP, Context.MODE_PRIVATE)
        accessCodeET.setText(sharedPref.getString(ACCESS_CODE_SP, ""))
    }

    fun loginOnClick(view : View)
    {
        swapVisibility(false, false)
        val accessCode = accessCodeET.text.toString()

        async(CommonPool)
        {
            val realm = Realm.getDefaultInstance()
            var gameData = realm.where(GameData::class.java).equalTo("accessCode",accessCode).findFirst();
            try
            {
                val response = retrofitApi.getLastChangeTimeStamp(accessCode).execute()
                if (response == null)
                {
                    makeToast(getString(R.string.connectionError))
                }
                else if (response.isSuccessful && gameData != null)
                {
                    if (gameData.changedTimestamp.toLong() < response.body().changedTimestamp)
                    {
                        val res = retrofitApi.getGameData(accessCode).execute()
                        if (res == null)
                        {
                            makeToast(getString(R.string.connectionError))
                        }
                        else if (res.isSuccessful)
                        {
                            realm.executeTransaction { realm.copyToRealmOrUpdate(res.body()) }
                            login()
                        }
                        else
                        {
                            makeToast(getString(R.string.errorExplained) + res.message())
                        }
                    }
                    else if (gameData.changedTimestamp.toLong() > response.body().changedTimestamp)
                    {
                        retrofitApi.postGameData(realm.copyFromRealm(gameData)).execute()
                        realm.executeTransaction { realm.copyToRealmOrUpdate(gameData) }
                        login()
                        realm.close()
                        return@async
                    }
                    else
                    {
                        login()
                        realm.close()
                        return@async
                    }
                }
                else if (response.isSuccessful )
                {
                    val res = retrofitApi.getGameData(accessCode).execute();
                    realm.executeTransaction { realm.copyToRealmOrUpdate(res.body()) }
                    login()
                    realm.close()
                    return@async
                }
                else if (response.raw().code() == 418)
                {
                    if (gameData == null)
                        gameData = GameData(accessCode)
                    val res = retrofitApi.postGameData(gameData).execute()
                    if (res.isSuccessful)
                    {
                        realm.executeTransaction { realm.copyToRealmOrUpdate(res.body()) }
                        login()
                        realm.close()
                        return@async
                    }
                }
                else if (response.raw().code() == 401)
                {
                    makeToast(getString(R.string.invalidAccessCode))
                }
                else
                {
                    makeToast(getString(R.string.serverError))
                }

                swapVisibility(true)
            }
            catch (ex: Exception)
            {
                makeToast(getString(R.string.connectionError))
                Log.d("Retrofit","Exception: "+ex.message)
                swapVisibility(true)
            }
            realm.close()
        }
    }

    private fun login()
    {
        mainThread(CommonPool)
        {
            val editor = sharedPref.edit()
            editor.putString(ACCESS_CODE_SP, accessCodeET.text.toString())
            editor.apply()

            swapVisibility(true, false)
            val realm = Realm.getDefaultInstance()
            Utils.gameData = realm.where(GameData::class.java).findFirst()
            startActivity(Intent(this, FlowersGalleryActivity::class.java))
        }
    }

    private fun swapVisibility(showButton: Boolean, threadSafe: Boolean = true)
    {
        if (threadSafe)
        {
            mainThread(CommonPool)
            {
                loginButton.visibility = if(showButton) View.VISIBLE else View.GONE
                progressBar.visibility = if(showButton) View.GONE else View.VISIBLE
            }
        }
        else
        {
            loginButton.visibility = if(showButton) View.VISIBLE else View.GONE
            progressBar.visibility = if(showButton) View.GONE else View.VISIBLE
        }
    }

    private fun makeToast(text: String)
    {
        mainThread(CommonPool)
        {
            toast(text)
        }
    }

}
