package martinek.segasesu.flowers

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_flowers_gallery.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import martinek.segasesu.R
import martinek.segasesu.Utils
import martinek.segasesu.Utils.flowersChanged
import martinek.segasesu.brain.BrainActivity
import martinek.segasesu.shop.ShopActivity
import java.util.concurrent.TimeUnit
import martinek.segasesu.MainActivity
import java.lang.Float.min


/**
 * Tabbed flower gallery, where player can see his flowers
 */
class FlowersGalleryActivity : AppCompatActivity()
{
    lateinit var realm: Realm

    var isRunning: Boolean = false
    var updater: Deferred<Unit>? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flowers_gallery)

        realm = Realm.getDefaultInstance()

        val sectionsPagerAdapter = FGPageAdapter(supportFragmentManager)

        flowersGaleryContainer.adapter = sectionsPagerAdapter
        flowersGaleryContainer.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state : Int) {}
            override fun onPageScrolled(position : Int, positionOffset : Float, positionOffsetPixels : Int) {}
            override fun onPageSelected(position : Int)
            {
                // Keeping state of focused flower for returning to specific tab
                Utils.changeFocusedFlower(position, realm)
            }
        })

        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)


        manageUpdater(true)
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_flowers_gallery, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        val id = item.itemId

        when (id)
        {
            R.id.action_brain ->
            {
                openBrain()
                return true
            }
            R.id.action_shop ->
            {
                openShop()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openBrain()
    {
        startActivity(Intent(this, BrainActivity::class.java))
    }

    fun openShop()
    {
        startActivity(Intent(this, ShopActivity::class.java))
    }

    override fun onPause()
    {
        manageUpdater(false)
        super.onPause()
    }

    override fun onResume()
    {
        manageUpdater(true)

        if (flowersChanged)
        {
            flowersGaleryContainer.adapter.notifyDataSetChanged()
            flowersChanged = false
        }
        flowersGaleryContainer.currentItem = Utils.gameData.focusedFlower
        super.onResume()
    }

    fun manageUpdater(run: Boolean)
    {
        if (run) {
            isRunning = true
            updater = async(CommonPool) {
                while (isRunning) {
                    decayAndLevel()
                    delay(30, TimeUnit.SECONDS)
                }
            }
        }
        else
        {
            isRunning = false
            updater = null
        }
    }

    fun decayAndLevel(){
        mainThread(CommonPool) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                for (flower in Utils.gameData.flowers) {
                    if (flower.lastDecay.isEmpty()) {
                        flower.lastDecay = (System.currentTimeMillis() / 1000L).toString()
                        continue
                    }

                    var lastDecay = flower.lastDecay.toLong()
                    val now = System.currentTimeMillis() / 1000L
                    while (now - lastDecay > 60) {
                        flower.expirience += (5F * flower.waterLevel / 100F * flower.mineralsLevel / 100F * flower.fertilizerLevel / 100F).toInt()
                        flower.state = Utils.getFlowerState(flower.expirience)

                        flower.waterLevel = Math.max(flower.waterLevel - min(5F,Math.abs(flower.mineralsLevel - flower.fertilizerLevel) / 2F - 2F), 1F)
                        flower.mineralsLevel = Math.max(flower.mineralsLevel - 2F, 1F)
                        flower.fertilizerLevel = Math.max(flower.fertilizerLevel - 5F, 1F)
                        lastDecay += 60
                        flower.lastDecay = lastDecay.toString()
                    }
                }

                Utils.gameData.changedTimestamp = "${System.currentTimeMillis() / 1000}"
            }

            Utils.postGameData(this@FlowersGalleryActivity, realm)
            flowersGaleryContainer.adapter.notifyDataSetChanged()
        }
    }

}
