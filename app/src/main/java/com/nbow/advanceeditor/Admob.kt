package com.nbow.advanceeditor

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.R
import com.google.android.gms.ads.FullScreenContentCallback







class Admob {


    companion object {
        var rewardedAd: RewardedAd? = null
        var fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.e("admob", "onAdShowedFullScreenContent: ", )
                    // Code to be invoked when the ad showed full screen content.
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.e("admob", "onAdDismissedFullScreenContent: ", )
                    rewardedAd = null
                    // Code to be invoked when the ad dismissed full screen content.
                }
            }

        fun loadAd(context: Context) {


            RewardedAd.load(
                context,
                "ca-app-pub-6948459060967328/3540124953",
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.e("Admob", "onAdLoaded: AD loaded", )
                        rewardedAd = ad
                        rewardedAd!!.setFullScreenContentCallback(fullScreenContentCallback)
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e("Admob", "onAdLoaded: AD loading failed", )
                        super.onAdFailedToLoad(p0)
                        loadAd(context)
                    }
                })
        }
    }

}
