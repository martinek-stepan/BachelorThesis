/*
 * Copyright 2017 Realm Inc.
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

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import martinek.segasesu.R

/**
 * Forms class and Category enum, Form include id, category and list of form items
 */
public open class Forms : RealmObject()
{

    @PrimaryKey
    var category : Int = 0
    var id: Int = 0

    var formItemList = RealmList<FormItem>()
}

enum class Category(val categoryNameId: Int, val tutorialId: Int)
{
    SELFREFLEXION(R.string.selfreflexionName, R.string.selfreflexion_tutorial),
    SELFSUPPORT(R.string.selfsupportName, R.string.selfsupport_tutorial),
    ACHIEVEMENTS(R.string.achievementsName, R.string.achievements_tutorial);

}
