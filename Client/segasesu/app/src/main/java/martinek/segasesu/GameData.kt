package martinek.segasesu

import android.support.annotation.DrawableRes
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import martinek.segasesu.brain.expandableForm.model.Forms

/**
 * Created by Kexik on 09.04.2017.
 */

/**
 * Contains all player game data
 */
open class GameData
(
    @PrimaryKey
    var accessCode: String = "",
    var playerName: String = "test player",
    var id: Int = 0,

    var flowers: RealmList<Flower> = RealmList(),
    var forms: RealmList<Forms> = RealmList(),
    var totalMoney: Int = 5,
    var focusedFlower: Int = 0,
    var changedTimestamp: String = ""

): RealmObject()

/**
 * Flower class contains single flower data
 */
open class Flower
(
        @PrimaryKey
        var id:  Int = 0,
        var flowerType: Int = FlowerTypes.BEAN.ordinal,

        var name : String = FlowerTypes.values()[flowerType].toString(),
        var waterLevel : Float = 100F,
        var mineralsLevel : Float = 100F,
        var fertilizerLevel : Float = 100F,
        var state : Int = FlowerStates.STATE_SPOUTS.ordinal,
        var expirience: Int = 0,
        var lastDecay: String = (System.currentTimeMillis()/1000L).toString()
) : RealmObject()
/**
 * Flower types enum with specific icons and grow images
 */
enum class FlowerTypes(@DrawableRes val icon: Int, @DrawableRes val images: IntArray)
{
    BEAN(R.mipmap.ic_bean_icon,intArrayOf(R.drawable.ic_b1, R.drawable.ic_b2, R.drawable.ic_b3, R.drawable.ic_b4)),
    CARROT(R.mipmap.ic_carrot_icon,intArrayOf(R.drawable.ic_m1, R.drawable.ic_m2, R.drawable.ic_m3, R.drawable.ic_m4)),
    PAPRIKA(R.mipmap.ic_paprika_icon,intArrayOf(R.drawable.ic_p1, R.drawable.ic_p2, R.drawable.ic_p3, R.drawable.ic_p4)),
    POTATO(R.mipmap.ic_potato_icon,intArrayOf(R.drawable.ic_b1, R.drawable.ic_b2, R.drawable.ic_b3, R.drawable.ic_b4));

    override fun toString() : String
    {
        return super.toString().toLowerCase().capitalize()
    }
}

enum class ResourceButtons(@DrawableRes val icons: IntArray)
{
    WATER(intArrayOf(R.drawable.ic_drop_1,R.drawable.ic_drop_2,R.drawable.ic_drop_3,R.drawable.ic_drop_4,R.drawable.ic_drop_5)),
    MINERALS(intArrayOf(R.drawable.ic_crystal_1,R.drawable.ic_crystal_2,R.drawable.ic_crystal_3,R.drawable.ic_crystal_4,R.drawable.ic_crystal_5)),
    POOP(intArrayOf(R.drawable.ic_poop_1,R.drawable.ic_poop_2,R.drawable.ic_poop_3,R.drawable.ic_poop_4,R.drawable.ic_poop_5)),
}

enum class FlowerStates
{
    STATE_SPOUTS,STATE_SMALL,STATE_MEDIUM,STATE_GROWN
}

object FlowerWarnings
{
    val LOW_MINERALS = 50F
    val LOW_FERTILIZER = 50F
}