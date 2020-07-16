package martinek.segasesu.brain.minigames

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_buttons_grid.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import martinek.segasesu.R
import martinek.segasesu.Utils
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong



/**
 * Game Matrix, player have to find all smiling faces in matrix in lowest possible time
 */

class MatrixActivity: ButtonsGridActivity(), View.OnClickListener
{
    var smiling = ArrayList<ImageButton>()
    var found = ArrayList<ImageButton>()
    var canClick = true
    var smilingNr = 4
    var playing = AtomicBoolean(true)
    var startTime = AtomicLong(System.currentTimeMillis())

    // Coroutine that updates timer
    val updater = async(CommonPool){
        while(playing.get())
        {
            updateCounter()
            delay(1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // calc nr of smiling faces
        smilingNr += rand.nextInt(3)

        // shuffle our list of buttons
        Collections.shuffle(buttons)

        // get smilling buttons
        smiling = ArrayList(buttons.subList(0,smilingNr))

        // set not smiling face and onclick listener to buttons
        for (butt in buttons) {
            butt.setImageResource(getRandomNotSmiling())
            butt.setOnClickListener(this);
        }

        // set smiling faces image
        for (smile in smiling)
            smile.setImageResource(getRandomSmiling())

        // update progress and counter according to data
        updateProgress()
        updateCounter()
    }


    override fun onClick(v: View) {
        // if player clicked not smiling face he is penalized for 1,5s
        if (!canClick)
            return;

        val ib = v as ImageButton
        // If smiling face, disable button add to found set backgound to green and if all found finish game
        if (smiling.contains(ib))
        {
            ib.isEnabled = false
            found.add(ib)
            ib.background.setColorFilter(Color.rgb(0, 128, 0), PorterDuff.Mode.SRC_ATOP)
            updateProgress()
            if (found.size == smilingNr)
                finishGame()
        }
        // Else set temporary red background and penalize player
        else
        {
            canClick = false
            ib.setImageResource(getRandomNotSmiling())
            ib.background.setColorFilter(Color.rgb(128, 0, 0), PorterDuff.Mode.SRC_ATOP)
            async(CommonPool)
            {
                delay(1500L)
                mainThread(CommonPool)
                {
                    ib.background.setColorFilter(null)
                    canClick = true
                }
            }
        }
    }

    override fun updateCounter() {
        mainThread(CommonPool){
                progressTV.text = getString(R.string.matrixTime, (System.currentTimeMillis()-startTime.get())/1000)
        }
    }

    override fun updateProgress() {
        counterTV.text = getString(R.string.minigameProgress,found.size,smilingNr)
    }


    // Shows dialog announcing win and nr of won coins
    private fun finishGame() {
        var reward = 1
        var msg = getString(R.string.matrixWin,(System.currentTimeMillis()-startTime.get())/100/10.0)
        if (Utils.isFirstWinOfDay(this, this@MatrixActivity.javaClass.simpleName))
        {
            reward += 4;
            msg += getString(R.string.firstWinOfDay)
        }
        else
            msg += "."

        playing.set(false)
        alert(msg)
        {
            yesButton {
                Utils.changeTotalMoney(reward,Realm.getDefaultInstance(), this@MatrixActivity)
                this@MatrixActivity.finish()
            }
            onCancelled {
                Utils.changeTotalMoney(reward,Realm.getDefaultInstance(), this@MatrixActivity)
                this@MatrixActivity.finish()
            }
        }.show()

    }

    override fun showTutorial(force: Boolean) {
        Utils.showTutorialDialog(this, this@MatrixActivity.javaClass.simpleName, getString(R.string.matrix_tutorial), force)
    }

}