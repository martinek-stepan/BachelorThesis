package martinek.segasesu.shop

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_shop.*
import martinek.segasesu.R
import martinek.segasesu.Utils
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


class ShopActivity : AppCompatActivity()
{

    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        realm = Realm.getDefaultInstance()

        coinsTV.text = "${Utils.gameData.totalMoney}"

        // Set icon and price of shop items
        for (item in Utils.shopItems)
            setIconAndPrice(item)
    }

    internal fun setIconAndPrice(item: ShopItem)
    {
        val linearLayout = this.findViewById(item.itemId) as LinearLayout
        val textView = linearLayout.findViewById(R.id.priceTV) as TextView
        textView.text = "${item.price}"

        if (item.imageResourceId != null)
        {
            val imageView = linearLayout.findViewById(R.id.shop_item_image) as ImageView
            imageView.setImageResource(item.imageResourceId)
        }

        linearLayout.setOnClickListener(ShopItemOnClickListener(item, coinsTV, realm))
    }
}


internal class ShopItemOnClickListener(var item: ShopItem, private val totalCoinsTV: TextView, private val realm: Realm) : View.OnClickListener
{

    override fun onClick(v: View)
    {
        // Check if player have money
        if (item.price > Utils.gameData.totalMoney)
        {
            v.context.toast(v.context.getString(R.string.notEnoughMoney))
            return
        }

        // Ask if he is sure
        v.context.alert(v.context.getString(R.string.buyConfirm, v.context.getString(item.nameRes)))
        {
            // Buy seed
            yesButton {
                totalCoinsTV.text = "${Utils.gameData.totalMoney-item.price}"
                Utils.addNewFlower(item.nameRes,item.price, realm, v.context)
                Toast.makeText(v.context, v.context.getString(R.string.buyed), Toast.LENGTH_SHORT).show()
                (v.context as ShopActivity).finish()
            }
            noButton {}
        }.show()
    }

}