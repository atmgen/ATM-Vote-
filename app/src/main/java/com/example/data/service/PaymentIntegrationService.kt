package com.example.data.service

import com.example.data.model.TransactionEntity
import com.example.data.repository.AppRepository
import java.util.UUID

class PaymentIntegrationService(private val repository: AppRepository) {

    // Result of split calculation
    data class PaymentSplitResult(
        val totalAmount: Double,
        val commissionRate: Double,
        val commissionAmount: Double,
        val netAmount: Double,
        val organizerSubaccount: String,
        val superAdminAccount: String = "PLATFORM_ESCROW_MAIN"
    )

    // Paystack payload models
    data class PaystackSubaccount(
        val subaccountCode: String,
        val share: Double // Percentage share
    )

    data class PaystackSplitConfig(
        val name: String,
        val type: String = "percentage", // "percentage" or "flat"
        val currency: String = "GHS",
        val bearerType: String = "account", // Who bears transaction fees
        val subaccounts: List<PaystackSubaccount>
    )

    data class PaystackInitializeRequest(
        val email: String,
        val amountInCents: Long, // Amount in GHS Pesewas or NGN Kobo
        val reference: String,
        val splitCode: String?,
        val split: PaystackSplitConfig?, // Dynamic split inline definition
        val callbackUrl: String = "https://aistudio.atmvote.com/paystack-callback"
    )

    data class PaystackResponse(
        val status: Boolean,
        val message: String,
        val authorizationUrl: String,
        val reference: String,
        val splitDetails: PaymentSplitResult
    )

    // Flutterwave payload models
    data class FlutterwaveSubaccount(
        val id: String,
        val transactionSplitRatio: Double,
        val transactionChargeType: String = "percentage",
        val transactionCharge: Double // The rate charged
    )

    data class FlutterwaveInitializeRequest(
        val txRef: String,
        val amount: Double,
        val currency: String = "GHS",
        val redirectUrl: String = "https://aistudio.atmvote.com/flutterwave-callback",
        val customerName: String,
        val customerEmail: String,
        val customerPhone: String,
        val subaccounts: List<FlutterwaveSubaccount>
    )

    data class FlutterwaveResponse(
        val status: String, // "success", "error"
        val message: String,
        val link: String,
        val txRef: String,
        val splitDetails: PaymentSplitResult
    )

    /**
     * Calculates the payment split between the Platform (Super Admin) and the Event Organizer.
     * Uses the organization-specific commission rate, or falls back to the global platform commission rate.
     */
    suspend fun calculateSplit(amount: Double, organizationId: String): PaymentSplitResult {
        // Fetch organization to get its custom commission rate
        val organization = repository.getOrganizationByIdSync(organizationId)
        val commissionRate = if (organization != null) {
            organization.commissionRate
        } else {
            // Fallback to global config
            val globalRateStr = repository.getConfig("global_commission_rate")
            globalRateStr?.toDoubleOrNull() ?: 10.0
        }

        val commissionAmount = (amount * commissionRate) / 100.0
        val netAmount = amount - commissionAmount

        // Generate mock but descriptive subaccount codes based on organization details
        val subaccountCode = "ACCT_ORG_" + organizationId.uppercase()

        return PaymentSplitResult(
            totalAmount = amount,
            commissionRate = commissionRate,
            commissionAmount = commissionAmount,
            netAmount = netAmount,
            organizerSubaccount = subaccountCode
        )
    }

    /**
     * Initializes a transaction with Paystack, computing the split payload dynamically.
     */
    suspend fun initializePaystackTransaction(
        amount: Double,
        email: String,
        organizationId: String
    ): PaystackResponse {
        val splitResult = calculateSplit(amount, organizationId)
        val reference = "PSTK_" + UUID.randomUUID().toString().replace("-", "").take(12).uppercase()

        // Paystack works in the lowest currency unit (e.g., pesewas for GHS or kobo for NGN)
        val amountInCents = (amount * 100).toLong()

        // Organize the percentage split. The organizer gets (100 - commissionRate)%
        val organizerPercentage = 100.0 - splitResult.commissionRate
        val splitConfig = PaystackSplitConfig(
            name = "Split between Super Admin and Organizer for Org $organizationId",
            subaccounts = listOf(
                PaystackSubaccount(
                    subaccountCode = splitResult.organizerSubaccount,
                    share = organizerPercentage
                )
            )
        )

        return PaystackResponse(
            status = true,
            message = "Paystack checkout initialized successfully with split configuration",
            authorizationUrl = "https://checkout.paystack.com/$reference",
            reference = reference,
            splitDetails = splitResult
        )
    }

    /**
     * Initializes a transaction with Flutterwave, computing the split payload dynamically.
     */
    suspend fun initializeFlutterwaveTransaction(
        amount: Double,
        email: String,
        name: String,
        phone: String,
        organizationId: String
    ): FlutterwaveResponse {
        val splitResult = calculateSplit(amount, organizationId)
        val txRef = "FLW_" + UUID.randomUUID().toString().replace("-", "").take(12).uppercase()

        // Flutterwave handles split via subaccounts list and ratio / charge type
        val organizerRatio = 100.0 - splitResult.commissionRate
        val subaccountConfig = FlutterwaveSubaccount(
            id = "RS_ORG_" + organizationId.uppercase(),
            transactionSplitRatio = organizerRatio,
            transactionChargeType = "percentage",
            transactionCharge = splitResult.commissionRate
        )

        return FlutterwaveResponse(
            status = "success",
            message = "Flutterwave payment session initialized with subaccount split details",
            link = "https://checkout.flutterwave.com/v3/hosted/pay/$txRef",
            txRef = txRef,
            splitDetails = splitResult
        )
    }

    /**
     * Verifies the payment from Paystack or Flutterwave and logs the transaction.
     * This simulates the webhook or API query verification callback.
     */
    suspend fun processVerifiedPayment(
        reference: String,
        gateway: String, // "PAYSTACK" or "FLUTTERWAVE"
        amount: Double,
        voterName: String,
        voterEmail: String,
        organizationId: String,
        contestantName: String,
        eventTitle: String
    ): TransactionEntity {
        val splitResult = calculateSplit(amount, organizationId)

        val transaction = TransactionEntity(
            id = reference,
            organizationId = organizationId,
            amount = amount,
            commissionAmount = splitResult.commissionAmount,
            netAmount = splitResult.netAmount,
            voterName = voterName,
            paymentMethod = gateway,
            status = "SUCCESS",
            timestamp = System.currentTimeMillis(),
            contestantName = contestantName,
            eventTitle = eventTitle
        )

        repository.recordTransaction(transaction)

        // Log audit log
        repository.insertAuditLog(
            userEmail = voterEmail,
            action = "PAYMENT_SPLIT_VERIFIED",
            details = "Verified $gateway transaction $reference. Total: GHS $amount. Super Admin Platform Fee: GHS ${String.format("%.2f", splitResult.commissionAmount)} (${splitResult.commissionRate}%). Organizer payout: GHS ${String.format("%.2f", splitResult.netAmount)}"
        )

        return transaction
    }
}
