/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package martinek.segasesu.brain.expandableForm.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_expandable_form.*
import martinek.segasesu.R
import martinek.segasesu.Utils
import martinek.segasesu.Utils.CATEGORY
import martinek.segasesu.Utils.EXPANDABLE_SHOW_COUNT
import martinek.segasesu.brain.expandableForm.model.Category
import martinek.segasesu.brain.expandableForm.model.FormItem
import martinek.segasesu.brain.expandableForm.model.Forms
import org.jetbrains.anko.toast

/**
 * Expandable form activity used for all task where player have to write something
 */
class ExpandableFormActivity : AppCompatActivity()
{

    private lateinit var realm : Realm
    private lateinit var adapter : ExapandableListRVAdapter
    private lateinit var category: Category

    private var menu: Menu? = null


    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable_form)
        realm = Realm.getDefaultInstance()

        val catNr = intent.getIntExtra(CATEGORY,-1)
        if (catNr == -1)
        {
            toast(getString(R.string.error))
            onBackPressed()
            return
        }
        category = Category.values()[catNr];

        supportActionBar?.title = category.toString()

        setUpRecyclerView(intent.getBooleanExtra(EXPANDABLE_SHOW_COUNT, false))
        Utils.showTutorialDialog(this, getString(category.categoryNameId), getString(category.tutorialId))
    }

    // Coin icon for mone indication
    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_coins_number, menu)
        this.menu = menu
        updateMenuMoney()
        return true
    }

    fun updateMenuMoney()
    {
        menu?.getItem(0)?.title = "${Utils.gameData.totalMoney}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.action_coins ->
            {
                toast(getString(R.string.currentCoins,Utils.gameData.totalMoney))
                return true
            }
            R.id.action_tutorial ->
            {
                Utils.showTutorialDialog(this, getString(category.categoryNameId), getString(category.tutorialId), true)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*
     * It is good practice to null the reference from the view to the adapter when it is no longer needed.
     * Because the <code>RealmRecyclerViewAdapter</code> registers itself as a <code>RealmResult.ChangeListener</code>
     * the view may still be reachable if anybody is still holding a reference to the <code>RealmResult>.
     */
    override fun onDestroy()
    {
        super.onDestroy()
        itemsRV.adapter = null
        realm.close()
    }

    private fun setUpRecyclerView(showCount: Boolean)
    {
        // Load form data or create new one
        var obj : Forms? = Utils.gameData.forms.where().equalTo("category", category.ordinal).findFirst()
        if (obj == null)
        {
            realm.beginTransaction()
            obj = realm.createObject(Forms::class.java, category.ordinal)
            Utils.gameData.forms.add(obj)
            realm.commitTransaction()
        }

        adapter = ExapandableListRVAdapter(obj?.formItemList !!, showCount, realm)
        itemsRV.layoutManager = LinearLayoutManager(this)
        itemsRV.adapter = adapter
        itemsRV.setHasFixedSize(true)
        itemsRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
    }

    fun newItemButtonClick(v : View)
    {
        switchVisibility(false)
    }

    fun addItemClick(v : View)
    {
        // If text is OK (right now only stupid checking by containt and !empty)
        if (checkText()) {

            realm.beginTransaction()
            // Get form
            val parent = Utils.gameData.forms.where().equalTo("category", category.ordinal).findFirst()
            val formItems = parent.formItemList
            // Create new item
            val formItem = realm.createObject(FormItem::class.java, newItemET.text.toString())
            formItem.id = formItems.size
            // Add new item
            formItems.add(formItem)
            realm.commitTransaction()

            val value = 1
            // Clear text
            newItemET.text.clear()
            // Notify adapter about new data
            adapter.notifyDataSetChanged()
            // Scroll to new data
            itemsRV.scrollToPosition(adapter.itemCount - 1)

            // Update money
            Utils.changeTotalMoney(value,realm, this)
            updateMenuMoney()
            toast(getString(R.string.gainCoin, value))
        }
        else
            toast(getString(R.string.doenstCount))
        switchVisibility(true)
    }

    private fun checkText(): Boolean {
        if (newItemET.text.isEmpty())
            return false;

        for(item in adapter.data !!)
            if (item.text == newItemET.text.toString())
                return false;
        return true;
    }

    fun switchVisibility(onlyButton: Boolean) {
        if (onlyButton) {
            newItemLL.visibility = View.GONE
            addItemButt.visibility = View.VISIBLE
        } else {
            newItemLL.visibility = View.VISIBLE
            addItemButt.visibility = View.GONE
            newItemET.requestFocus()
        }
    }
}

