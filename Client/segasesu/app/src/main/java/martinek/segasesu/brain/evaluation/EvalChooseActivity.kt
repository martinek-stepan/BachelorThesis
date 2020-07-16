package martinek.segasesu.brain.evaluation

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.yoavst.kotlin.`KotlinPackage$Tasks$65a6a183`.mainThread
import kotlinx.android.synthetic.main.activity_eval_choose.*
import kotlinx.coroutines.experimental.CommonPool
import martinek.segasesu.R
import martinek.segasesu.Utils
import martinek.segasesu.Utils.CATEGORY
import martinek.segasesu.Utils.gameData
import martinek.segasesu.brain.EvaluationRequest
import martinek.segasesu.brain.expandableForm.model.Category
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Allow you to chose what do you wanna evaluate
 */
class EvalChooseActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eval_choose)
        Utils.evaluation = null
    }

    fun EvalSelfReflexion(v: View)
    {
        load(Category.SELFREFLEXION)
    }

    fun EvalSelfSupport(v: View)
    {
        load(Category.SELFSUPPORT)
    }

    // When chosen loads data from server
    private fun load(category: Category)
    {
        showLoading(true)
        Utils.retrofitApi.getEvaluationList(gameData.id, category.ordinal).enqueue(object: Callback<EvaluationRequest>
        {
            // Connection error cant evaluate anzthing
            override fun onFailure(call : Call<EvaluationRequest>?, t : Throwable?)
            {
                mainThread(CommonPool)
                {
                    toast(getString(R.string.connectionError))
                    showLoading(false)
                }
            }

            override fun onResponse(call : Call<EvaluationRequest>?, response : Response<EvaluationRequest>)
            {
                mainThread(CommonPool)
                {
                    showLoading(false)
                    // Server error cant evaluate anything
                    if (! response.isSuccessful)
                    {
                        toast(getString(R.string.serverError))
                    }
                    // Server doesnt have enoug data...
                    else if (response.raw().code() == 204)
                    {
                        toast(getString(R.string.notEnoughData))
                    }
                    // OK start activity
                    else
                    {
                        val intent = Intent(this@EvalChooseActivity, EvaluationActivity::class.java)
                        intent.putExtra(CATEGORY, category.ordinal)
                        Utils.evaluation = response.body();
                        startActivity(intent)
                    }
                }
            }
        })
    }

    // Show or hide loading progress
    private fun showLoading(show: Boolean)
    {
        evalChooseLoadingLL.visibility = if (show) View.VISIBLE else View.GONE;
    }
}
