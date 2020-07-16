package martinek.segasesu

import android.app.Application
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmConfiguration
import martinek.segasesu.shop.ShopItem
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by smartinek on 3.4.2017.
 */

class App : Application()
{

    override fun onCreate() {
        super.onCreate()

        // Initialize Realm
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .build()
        Realm.deleteRealm(realmConfig) // Delete Realm between app restarts.
        //Realm.setDefaultConfiguration(realmConfig)

        // Add shop items
        Utils.shopItems.add(ShopItem(R.string.seed_random, 5, R.mipmap.ic_random_icon, R.id.shop_item_random_seed))
        Utils.shopItems.add(ShopItem(R.string.seed_paprika, 7, R.mipmap.ic_paprika_icon, R.id.shop_item_seed_paprika))
        Utils.shopItems.add(ShopItem(R.string.seed_bean, 8, R.mipmap.ic_bean_icon, R.id.shop_item_seed_bean))
        Utils.shopItems.add(ShopItem(R.string.seed_potato, 10, R.mipmap.ic_potato_icon, R.id.shop_item_seed_potato))
        Utils.shopItems.add(ShopItem(R.string.seed_carrot, 12, R.mipmap.ic_carrot_icon, R.id.shop_item_seed_carrot))

        // Initialize Gson for parsing JSONs
        val gson = GsonBuilder().create()
        val httpClientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG)
        {
            // enable logging for debug builds
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }

        // Initialize retrofit
        Utils.retrofitApi = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build())
                .build().create<RetrofitApi>(RetrofitApi::class.java)
    }


}