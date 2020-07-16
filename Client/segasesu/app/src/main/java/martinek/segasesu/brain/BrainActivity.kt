package martinek.segasesu.brain

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import martinek.segasesu.R
import martinek.segasesu.Utils.CATEGORY
import martinek.segasesu.Utils.EXPANDABLE_SHOW_COUNT
import martinek.segasesu.brain.evaluation.EvalChooseActivity
import martinek.segasesu.brain.expandableForm.model.Category
import martinek.segasesu.brain.expandableForm.ui.ExpandableFormActivity
import martinek.segasesu.brain.minigames.MatrixActivity
import martinek.segasesu.brain.minigames.PexesoActivity


/**
 * Simple activity with buttons to start specific task or minigame
 */
class BrainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_brain)
    }

    fun Matrix(v: View)
    {
        startActivity(Intent(this, MatrixActivity::class.java))
    }

    fun Pexeso(v: View)
    {
        startActivity(Intent(this, PexesoActivity::class.java))
    }

    fun SelfReflection(v: View)
    {
        val i = Intent(this, ExpandableFormActivity::class.java)
        i.putExtra(CATEGORY,Category.SELFREFLEXION.ordinal)
        i.putExtra(EXPANDABLE_SHOW_COUNT, false)
        startActivity(i)
    }

    fun SelfSupport(v: View)
    {
        val i = Intent(this, ExpandableFormActivity::class.java)
        i.putExtra(CATEGORY,Category.SELFSUPPORT.ordinal)
        i.putExtra(EXPANDABLE_SHOW_COUNT, true)
        startActivity(i)
    }

    fun Achievements(v: View)
    {
        val i = Intent(this, ExpandableFormActivity::class.java)
        i.putExtra(CATEGORY,Category.ACHIEVEMENTS.ordinal)
        i.putExtra(EXPANDABLE_SHOW_COUNT, false)
        startActivity(i)
    }

    fun evaluation(v: View)
    {
        startActivity(Intent(this, EvalChooseActivity::class.java))
    }
}
