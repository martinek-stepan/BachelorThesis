package martinek.segasesu.brain.minigames

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_buttons_grid.*
import martinek.segasesu.R
import martinek.segasesu.Utils
import java.util.*

/**
 * Base activity that containts grid of buttons, score counter text view and progress textview
 */

abstract class ButtonsGridActivity : AppCompatActivity() {

    var buttons = ArrayList<ImageButton>()
    protected val smilingFaces = intArrayOf(R.drawable.ic_smiling_1,R.drawable.ic_smiling_2,R.drawable.ic_smiling_3,R.drawable.ic_smiling_4,R.drawable.ic_smiling_5)
    protected val notSmilingFaces = intArrayOf(R.drawable.ic_not_smiling_1,R.drawable.ic_not_smiling_2,R.drawable.ic_not_smiling_3,R.drawable.ic_not_smiling_4,R.drawable.ic_not_smiling_5,R.drawable.ic_not_smiling_6,R.drawable.ic_not_smiling_7,R.drawable.ic_not_smiling_8)

    protected val rand = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buttons_grid)

        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        val width = displaymetrics.widthPixels

        val buttonSide = width / 5

        for (i in 0..4)
        {
            for (j in 0..4)
            {
                val ib = ImageButton(this)
                ib.setImageResource(R.drawable.ic_brain)
                ib.setScaleType(ImageView.ScaleType.FIT_XY);
                ib.layoutParams = ViewGroup.LayoutParams(buttonSide,buttonSide)
                buttons.add(ib)
                buttonsGL.addView(ib)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_tutorial, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        val id = item.itemId

        when (id)
        {
            R.id.action_tutorial ->
            {
                showTutorial(true);
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    protected fun getRandomNotSmiling() = notSmilingFaces[rand.nextInt(notSmilingFaces.size)]

    protected fun getRandomSmiling() = smilingFaces[rand.nextInt(smilingFaces.size)]


    abstract fun updateCounter()

    abstract fun updateProgress()

    abstract fun showTutorial(force: Boolean = false)
}
