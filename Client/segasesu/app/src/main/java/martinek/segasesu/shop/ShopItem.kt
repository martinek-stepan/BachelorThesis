package martinek.segasesu.shop

import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes


/**
 * Created by Kexik on 12.03.2017.
 * Contains shop item data
 */

class ShopItem(var nameRes : @param:StringRes Int,
                        var price :  Int,
                        val imageResourceId : @param:DrawableRes Int?,
                        var itemId : @param:IdRes Int)
