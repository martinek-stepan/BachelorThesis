package martinek.segasesu.brain.evaluation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_evaluation.*
import kotlinx.android.synthetic.main.evaluation_row.view.*
import kotlinx.coroutines.experimental.CommonPool
import martinek.segasesu.R
import martinek.segasesu.Utils
import martinek.segasesu.Utils.retrofitApi
import martinek.segasesu.brain.EvaluationRequest
import martinek.segasesu.brain.EvaluationResponse
import martinek.segasesu.brain.EvaluationResponseRow
import org.honorato.multistatetogglebutton.MultiStateToggleButton
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EvaluationActivity : AppCompatActivity()
{

    lateinit var eval: EvaluationRequest
    var map: MutableMap<Int, MultiStateToggleButton> = HashMap()
    var category = 0

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluation)

        // Get category
        category = intent.getIntExtra(Utils.CATEGORY,-1)
        if (Utils.evaluation == null || category == -1)
        {
            toast(getString(R.string.error))
            onBackPressed()
        }

        eval = Utils.evaluation !!

        // Add evaluations into view
        for (row in eval.rows)
        {
            val v = layoutInflater.inflate(R.layout.evaluation_row, itemsLL)
            v.evaluationRowTV.text = row.text
            map.put(row.id, v.mstb_multi_id)
        }

        Utils.showTutorialDialog(this, "EvaluationTutorial", getString(R.string.evaluation_tutorial))

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
                Utils.showTutorialDialog(this, "EvaluationTutorial", getString(R.string.evaluation_tutorial), true)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
    // Tries to send evaluation to server
    fun sendEvaluation(v: View)
    {
        val list = ArrayList<EvaluationResponseRow>()
        val response = EvaluationResponse(list)
        // Extract valuation results, prevent player sending incomplete evaluation
        for(pair in map)
        {
            val selected = pair.value.value
            if (selected == -1)
            {
                toast(getString(R.string.missingEvaluations))
                return
            }
            list.add(EvaluationResponseRow(pair.key, selected))
        }

        showLoading(true)

        // Post evaluation
        retrofitApi.postEvaluation(response, Utils.gameData.id, category).enqueue(object: Callback<Void>
        {
            override fun onResponse(call : Call<Void>?, response : Response<Void>)
            {
                mainThread(CommonPool)
                {

                    showLoading(false)
                    if (!response.isSuccessful)
                    {
                        toast(getString(R.string.serverError))
                    }
                    else
                    {
                        val reward = 5
                        Utils.changeTotalMoney(reward, Realm.getDefaultInstance(), this@EvaluationActivity)
                        toast(getString(R.string.gainCoin,reward))
                        onBackPressed()
                    }
                }
            }

            override fun onFailure(call : Call<Void>?, t : Throwable?)
            {
                mainThread(CommonPool)
                {
                    showLoading(false)
                    toast(getString(R.string.connectionError))
                }
            }
        })
    }

    private fun showLoading(show: Boolean)
    {
        evalLoadingLL.visibility = if (show) View.VISIBLE else View.GONE;
    }
}
