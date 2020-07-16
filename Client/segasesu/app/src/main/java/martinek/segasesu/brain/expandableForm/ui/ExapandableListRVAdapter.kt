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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import martinek.segasesu.R
import martinek.segasesu.Utils
import martinek.segasesu.brain.expandableForm.model.FormItem

/**
 * Expandable list adabter, setting content of row view
 */
internal class ExapandableListRVAdapter(data : OrderedRealmCollection<FormItem>, private val showButtons : Boolean, private val realm : Realm) : RealmRecyclerViewAdapter<FormItem, ExapandableListRVAdapter.MyViewHolder>(data, true)
{

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : MyViewHolder
    {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.expandable_form_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder : MyViewHolder, position : Int)
    {
        val obj = getItem(position)

        //TODO new view for position
        holder.text.text = "${position+1}.\t\t\t${obj !!.text}"
        if (! showButtons)
            holder.countButt.visibility = GONE
        else
        {
            holder.countButt.visibility = View.VISIBLE
            holder.countButt.text = obj.countString
            holder.countButt.setOnClickListener {
                realm.beginTransaction()
                obj.inc()
                realm.commitTransaction()
                Utils.postGameData(holder.countButt.context,realm);
            }
        }
    }

    override fun getItemId(index : Int) : Long
    {

        return getItem(index) !!.count.toLong()
    }

    internal inner class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)
    {
        var text : TextView
        var countButt : Button

        init
        {
            text = view.findViewById(R.id.itemTextTV) as TextView
            countButt = view.findViewById(R.id.itemCounterButt) as Button
        }
    }
}