package martinek.segasesu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.support.annotation.StringRes
import android.util.Log
import android.widget.CheckBox
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import io.realm.Realm
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import martinek.segasesu.R.id.textView
import martinek.segasesu.brain.EvaluationRequest
import martinek.segasesu.shop.ShopItem
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.abs
import java.lang.Math.max
import java.util.*

/**
 * Created by smartinek on 3.3.2017.
 */

object Utils
{
    lateinit var retrofitApi : RetrofitApi

    val CATEGORY = "CATEGORY"
    val EXPANDABLE_SHOW_COUNT = "EXPANDABLE_SHOW_COUNT"
    val ACCESS_CODE_SP = "ACCESS_CODE"
    val SEGASESU_SP = "SEGASESU"


    lateinit var gameData : GameData
    var flowersChanged = false
    var shopItems: MutableList<ShopItem> = ArrayList()


    /**
     * Post helper for server comunication
     */
    fun postGameData(context: Context, realm: Realm)
    {
        retrofitApi.postGameData(realm.copyFromRealm(gameData)).enqueue(object: Callback<GameData>
        {
            override fun onFailure(call : Call<GameData>?, t : Throwable?)
            {
                // ignore connection failures
            }

            override fun onResponse(call : Call<GameData>?, response : Response<GameData>)
            {
                mainThread(CommonPool)
                {
                    if (!response.isSuccessful)
                    {
                        context.toast(context.getString(R.string.dataMismatch))
                        val intent = Intent(context.applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(Intent(context.applicationContext, MainActivity::class.java))
                    }
                    else
                    {
                        realm.executeTransaction { realm.insertOrUpdate(response.body()) }
                    }
                }
            }
        })
    }

    /**
     * Money change helper
     */
    fun  changeTotalMoney(moneyChange : Int, realm : Realm, context: Context)
    {
        realm.beginTransaction()
        gameData.totalMoney += moneyChange
        realm.commitTransaction()
        postGameData(context, realm)
    }

    /**
     * New flower creating helper
     */
    fun addNewFlower(nameRes : @param:StringRes Int, price: Int, realm : Realm, context: Context)
    {
        realm.beginTransaction()
        gameData.totalMoney -= price
        changeFocusedFlower(gameData.flowers.size)
        val type = getFlowerType(nameRes)
        val flower = Flower(gameData.flowers.size,type.ordinal)
        gameData.flowers.add(flower)
        realm.commitTransaction()
        postGameData(context, realm)
    }

    /**
     * Gets flower type from seed string (or choose random flower if random seed)
     */
    private fun getFlowerType(nameRes : @param:StringRes Int) : FlowerTypes
    {
        var name = nameRes
        if (nameRes == R.string.seed_random)
        {
            val rand = Random()
            val number = rand.nextInt(100)
            when(number)
            {
                in 0..5 -> name = R.string.seed_paprika
                in 6..15 -> name = R.string.seed_carrot
                in 16..45 -> name = R.string.seed_bean
                else -> name = R.string.seed_potato
            }
        }
        when(name)
        {
            R.string.seed_potato -> return FlowerTypes.POTATO
            R.string.seed_carrot -> return FlowerTypes.CARROT
            R.string.seed_bean -> return FlowerTypes.BEAN
            R.string.seed_paprika -> return FlowerTypes.PAPRIKA
        }
        return FlowerTypes.POTATO
    }

    /**
     * Helper for keeping opened index to return on correct tab
     */
    fun changeFocusedFlower(position : Int, realm : Realm? = null)
    {
        if (realm == null)
            gameData.focusedFlower = position
        else
            realm.executeTransaction { gameData.focusedFlower = position }
        flowersChanged = true
    }

    var evaluation : EvaluationRequest? = null



    // Prototype for generic tutorial
    fun showTutorialDialog(context: Context, className: String, tutorialText: String, force: Boolean = false)
    {
        val sharedPref = context.getSharedPreferences(Utils.SEGASESU_SP, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(className, false) && !force)
            return;

        context.alert {
            var checkBox: CheckBox? = null
            customView {
                verticalLayout {
                    textView {
                        text = tutorialText
                    }.lparams(width = matchParent, height = wrapContent) {
                        margin = dip(10)
                    }

                    checkBox = checkBox(context.getString(R.string.dontShowAgain), sharedPref.getBoolean(className, false))
                }
                yesButton {
                    saveTutorialSetting(sharedPref, className, checkBox?.isChecked ?: false)
                }
                onCancelled {
                    //saveTutorialSetting(sharedPref, className, checkBox?.isChecked ?: false)
                }
            }
        }.show()
    }

    private fun saveTutorialSetting(sharedPref: SharedPreferences, className: String, checked: Boolean)
    {
        val editor = sharedPref.edit()
        editor.putBoolean(className, checked)
        editor.apply()
    }

    fun isFirstWinOfDay(context: Context, className: String): Boolean
    {
        val calendar: Calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val sharedPref = context.getSharedPreferences(Utils.SEGASESU_SP, Context.MODE_PRIVATE)
        val savedDay = sharedPref.getInt(className+"FirstWin", -1)

        if (savedDay != dayOfMonth)
        {
            val editor = sharedPref.edit()
            editor.putInt(className+"FirstWin", dayOfMonth)
            editor.apply()
            return true
        }
        return false
    }

    fun getFlowerState(xp: Int): Int
    {
        when(xp)
        {
            in 0..300 -> return FlowerStates.STATE_SPOUTS.ordinal
            in 301..450 -> return FlowerStates.STATE_SMALL.ordinal
            in 451..600 -> return FlowerStates.STATE_MEDIUM.ordinal
            else -> return FlowerStates.STATE_GROWN.ordinal // full grown after 5 hours in ideal case
        }
    }

    fun getLevelProgress(xp: Int): Int
    {
        when(xp)
        {
            in 0..300 -> return (xp / 300.0*100).toInt()
            in 301..450 -> return ((xp - 300) / 150.0*100).toInt()
            in 451..600 -> return ((xp - 450) / 150.0*100).toInt()
            else -> return 100 // full grown after 5 hours in ideal case
        }
    }

}
