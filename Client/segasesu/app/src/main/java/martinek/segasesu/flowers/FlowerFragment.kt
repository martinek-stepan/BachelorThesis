package martinek.segasesu.flowers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_flowers_gallery.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import martinek.segasesu.FlowerTypes
import martinek.segasesu.R
import martinek.segasesu.ResourceButtons
import martinek.segasesu.Utils
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.lang.Integer.min
import java.util.concurrent.atomic.AtomicBoolean
import android.R.id.button1
import android.R.attr.x
import android.util.DisplayMetrics
import android.view.Display



/**
 * Created by smartinek on 3.3.2017.
 * Retrieve flower data and build UI
 */
class FlowerFragment(val position: Int) : Fragment()
{
    lateinit var realm: Realm

    lateinit var waterButton: ImageButton
    lateinit var mineralsButton: ImageButton
    lateinit var fertilityButton: ImageButton
    lateinit var xpPB: ProgressBar

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        val rootView = inflater.inflate(R.layout.fragment_flowers_gallery, container, false)
        val data = Utils.gameData.flowers[position]

        realm = Realm.getDefaultInstance()
        val flowerImage = rootView.findViewById(R.id.flowerImage) as ImageView
        flowerImage.setImageResource(FlowerTypes.values()[data.flowerType].images[data.state])

        val textView = rootView.findViewById(R.id.section_label) as TextView
        textView.text = data.name

        xpPB = rootView.findViewById(R.id.xpPB) as ProgressBar
        val progress = Utils.getLevelProgress(Utils.gameData.flowers[position].expirience)
        xpPB.setProgress(progress)

        val price = 3

        waterButton = rootView.findViewById(R.id.waterIB) as ImageButton
        // On click offers replenish water with alert dialog
        waterButton.setOnClickListener {v ->
            v.context.alert(v.context.getString(R.string.refillQuestionWater,price))
            {
                yesButton {
                    if (Utils.gameData.totalMoney < price)
                    {
                        v.context.toast(v.context.getString(R.string.notEnoughMoney))
                        return@yesButton
                    }
                    realm.executeTransaction { Utils.gameData.flowers[Utils.gameData.focusedFlower].waterLevel = 100F }
                    Utils.changeTotalMoney(-price, realm, v.context)
                    v.context.toast(v.context.getString(R.string.refilled))
                    update()
                }
                noButton {}
            }.show()
        }


        mineralsButton = rootView.findViewById(R.id.mineralsIB) as ImageButton
        // On click offers replenish mirnals with alert dialog
        mineralsButton.setOnClickListener {v ->
            v.context.alert(v.context.getString(R.string.refillQuestionMinerals,price))
            {
                yesButton {
                    if (Utils.gameData.totalMoney < price)
                    {
                        v.context.toast(v.context.getString(R.string.notEnoughMoney))
                        return@yesButton
                    }
                    realm.executeTransaction { Utils.gameData.flowers[Utils.gameData.focusedFlower].mineralsLevel = 100F }
                    Utils.changeTotalMoney(-price, realm, v.context)
                    v.context.toast(v.context.getString(R.string.refilled))
                    update()
                }
                noButton {}
            }.show()
        }

        fertilityButton = rootView.findViewById(R.id.fertilizerIB) as ImageButton
        // On click offers replenish fertelizer with alert dialog
        fertilityButton.setOnClickListener {v ->
            v.context.alert(v.context.getString(R.string.refillQuestionPoop,price))
            {
                yesButton {
                    if (Utils.gameData.totalMoney < price)
                    {
                        v.context.toast(v.context.getString(R.string.notEnoughMoney))
                        return@yesButton
                    }
                    realm.executeTransaction { Utils.gameData.flowers[Utils.gameData.focusedFlower].fertilizerLevel = 100F }
                    Utils.changeTotalMoney(-price, realm, v.context)
                    v.context.toast(v.context.getString(R.string.refilled))
                    update()
                }
                noButton {}
            }.show()
        }

        update()
        return rootView
    }

    fun update()
    {
        val data = Utils.gameData.flowers[position]
        val progress = Utils.getLevelProgress(Utils.gameData.flowers[position].expirience)
        xpPB.setProgress(progress)
        waterButton.setImageResource(ResourceButtons.WATER.icons[min(4,data.waterLevel.toInt()/20)])
        waterButton.setScaleType(ImageView.ScaleType.FIT_XY);
        mineralsButton.setImageResource(ResourceButtons.MINERALS.icons[min(4,data.mineralsLevel.toInt()/20)])
        mineralsButton.setScaleType(ImageView.ScaleType.FIT_XY);
        fertilityButton.setImageResource(ResourceButtons.POOP.icons[min(4,data.fertilizerLevel.toInt()/20)])
        fertilityButton.setScaleType(ImageView.ScaleType.FIT_XY);
    }
}
