package com.thatapp.checklist.ModelClasses

import android.content.Context
import com.android.billingclient.api.*

class Billing (context: Context):PurchasesUpdatedListener{

	private var billingClient: BillingClient

	init {
		billingClient = BillingClient.newBuilder(context).setListener(this).build()
		setBilling()
	}

	private fun setBilling() {
		billingClient.startConnection(object : BillingClientStateListener {
			override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
				if (billingResponseCode == BillingClient.BillingResponse.OK) {


				}
			}

			override fun onBillingServiceDisconnected() {
				// Try to restart the connection on the next request to
				// Google Play by calling the startConnection() method.
			}
		})
	}

	override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
		if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
			for (purchase in purchases) {
//				handlePurchase(purchase)
			}
		} else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
			// Handle an error caused by a user cancelling the purchase flow.
		} else {
			// Handle any other error codes.
		}
	}

	private fun skuQuery(){
		val skuList = ArrayList<String>()
		skuList.add("checklist1370")

		val params = SkuDetailsParams.newBuilder()
		params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
		billingClient.querySkuDetailsAsync(params.build(), { responseCode, skuDetailsList ->
			// Process the result.
			for (skuDetails in skuDetailsList) {
				val sku = skuDetails.sku
				val price = skuDetails.price
				if ("checklist1370" == sku) {
//					premiumUpgradePrice = price
				}
			}
		})
	}

}