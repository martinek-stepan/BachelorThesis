package martinek.segasesu.brain.minigames

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_buttons_grid.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import martinek.segasesu.R
import martinek.segasesu.Utils
import org.jetbrains.anko.*
import java.util.*

/**
 * Created by smartinek on 18.4.2017.
 */

class PexesoActivity: ButtonsGridActivity(), View.OnClickListener
{
    var smiling = ArrayList<ImageButton>()
    var found = ArrayList<ImageButton>()
    var canClick = true
    var smilingNr = 5
    var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // generate nr of smiling faces
        smilingNr += rand.nextInt(5)

        // add smiling buttons to specific list
        smiling.addAll(buttons)
        Collections.shuffle(smiling)
        smiling = ArrayList(smiling.subList(0,smilingNr))

        // set on click listener
        for (butt in buttons)
            butt.setOnClickListener(this);

        // get SP and show tutorial if wanted
        showTutorial()

        updateProgress()
        updateCounter()
    }

    override fun onClick(v: View) {
        // if player clicked not smiling face he is penalized for 1,5s
        if (!canClick)
            return;

        clickCount++;
        updateCounter()
        val ib = v as ImageButton
        // If turned smiling face change image add to found, update progress and finish game if all
        if (smiling.contains(ib))
        {
            ib.isEnabled = false
            found.add(ib)
            ib.setImageResource(getRandomSmiling())
            updateProgress()
            if (found.size == smilingNr)
                finishGame()
        }
        // Turn background red, after delay turn back all yet found faces
        else
        {
            canClick = false
            ib.setImageResource(getRandomNotSmiling())
            ib.background.setColorFilter(Color.rgb(128, 0, 0), PorterDuff.Mode.SRC_ATOP)
            found.add(ib)
            async(CommonPool)
            {
                delay(1500L)
                mainThread(CommonPool)
                {
                    ib.background.setColorFilter(null)
                    found.forEach {
                        it.isEnabled = true
                        it.setImageResource(R.drawable.ic_brain)
                    }
                    found.clear()
                    canClick = true
                    updateProgress()
                }
            }
        }
    }

    override fun showTutorial(force: Boolean) {
        Utils.showTutorialDialog(this, this@PexesoActivity.javaClass.simpleName, getString(R.string.pexeso_tutorial), force)
    }

    override fun updateCounter() {
        progressTV.text = getString(R.string.pexesoTurns,clickCount)
    }

    override fun updateProgress() {
        counterTV.text = getString(R.string.minigameProgress,found.size,smilingNr)
    }

    // Shows dialog announcing win and nr of won coins
    private fun finishGame() {
        var reward = 1
        var msg = getString(R.string.pexesoWin,clickCount)
        if (Utils.isFirstWinOfDay(this, this@PexesoActivity.javaClass.simpleName))
        {
            reward += 4;
            msg += getString(R.string.firstWinOfDay)
        }
        else
            msg += "."

        alert(msg)
        {
            yesButton {
                Utils.changeTotalMoney(reward,Realm.getDefaultInstance(), this@PexesoActivity)
                this@PexesoActivity.finish()
            }
            onCancelled {
                Utils.changeTotalMoney(reward,Realm.getDefaultInstance(), this@PexesoActivity)
                this@PexesoActivity.finish()
            }
        }.show()

    }

}