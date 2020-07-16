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
package martinek.segasesu.brain.expandableForm.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * FormItem class inglude text, id, and count (used only in some forms)
 */
open class FormItem
(
    @PrimaryKey
    var text : String? = null,
    var id: Int = 0,
    var count: Int = 1

) : RealmObject()
{

    val countString : String
        get() = Integer.toString(count)

    fun inc()
    {
        count ++
    }
}
