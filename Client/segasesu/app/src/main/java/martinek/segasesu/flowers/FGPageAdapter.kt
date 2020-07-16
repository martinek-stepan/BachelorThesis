package martinek.segasesu.flowers

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import martinek.segasesu.Utils

/**
 * Created by smartinek on 3.3.2017.
 */
/**
 * Fragment page adapter
 */
internal class FGPageAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm)
{
    override fun getItem(position : Int) : Fragment
    {
        return FlowerFragment(position)
    }

    override fun getItemPosition(fragment: Any?): Int {
        if (fragment is FlowerFragment)
            fragment.update()
        return super.getItemPosition(fragment)
    }

    override fun getCount() = Utils.gameData.flowers.size
    override fun getPageTitle(position : Int) = Utils.gameData.flowers[position].name


}
