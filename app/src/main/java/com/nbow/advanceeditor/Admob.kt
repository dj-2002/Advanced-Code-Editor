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
import com.google.android.gms.ads.interstitial.InterstitialAd
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdError

import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class Admob {




    companion object {
        var rewardedAd: RewardedAd? = null
        var mInterstitialAd: InterstitialAd? = null
        private  val TAG = "Admob"
        var fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.e("admob", "onAdShowedFullScreenContent: ")
                    // Code to be invoked when the ad showed full screen content.
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.e("admob", "onAdDismissedFullScreenContent: ")
                    rewardedAd = null
                    // Code to be invoked when the ad dismissed full screen content.
                }
            }

        var interstitialAdLoadCallback: FullScreenContentCallback = object : FullScreenContentCallback(){
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
            }

            override fun onAdShowedFullScreenContent() {
                mInterstitialAd = null;
                super.onAdShowedFullScreenContent()
            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null;
                super.onAdDismissedFullScreenContent()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }
        }
        fun loadRewardedAd(context: Context) {


            RewardedAd.load(
                context,
                "ca-app-pub-3940256099942544/5224354917",
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.e("Admob", "onAdLoaded: AD loaded")
                        rewardedAd = ad
                        rewardedAd!!.setFullScreenContentCallback(fullScreenContentCallback)
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e("Admob", "onAdLoaded: AD loading failed")
                        super.onAdFailedToLoad(p0)
                        loadRewardedAd(context)
                    }
                })
        }

        fun loadInterstitialAd(context: Context)
        {
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        Log.e(TAG, "onAdLoaded")
                        mInterstitialAd?.fullScreenContentCallback= interstitialAdLoadCallback
                    }
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, loadAdError.message)
                        mInterstitialAd = null
                        loadInterstitialAd(context)
                    }
                })
        }
    }

}
