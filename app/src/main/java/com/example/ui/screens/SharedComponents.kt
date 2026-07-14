package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintGreen
import com.example.ui.viewmodel.VotePackage
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CountdownTimer(targetTimestamp: Long, modifier: Modifier = Modifier) {
    var timeLeft by remember { mutableLongStateOf(targetTimestamp - System.currentTimeMillis()) }

    LaunchedEffect(key1 = targetTimestamp) {
        while (timeLeft > 0) {
            timeLeft = targetTimestamp - System.currentTimeMillis()
            delay(1000)
        }
    }

    val days = (timeLeft / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    val hours = ((timeLeft / (1000 * 60 * 60)) % 24).coerceAtLeast(0)
    val minutes = ((timeLeft / (1000 * 60)) % 60).coerceAtLeast(0)
    val seconds = ((timeLeft / 1000) % 60).coerceAtLeast(0)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimerUnit(value = days, label = "Days")
        Text(":", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TimerUnit(value = hours, label = "Hrs")
        Text(":", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TimerUnit(value = minutes, label = "Mins")
        Text(":", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TimerUnit(value = seconds, label = "Secs")
    }
}

@Composable
fun RowScope.TimerUnit(value: Long, label: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%02d", value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun VerificationCaptchaDialog(
    packageItem: VotePackage,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var captchaNum1 by remember { mutableStateOf((2..8).random()) }
    var captchaNum2 by remember { mutableStateOf((2..9).random()) }
    var captchaAnswerInput by remember { mutableStateOf("") }
    var userOtpInput by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voter Secure Gateway", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "To prevent automated sybil voting, duplicate entries, and web scraping, please pass security validations below.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // CAPTCHA Block
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("1. Solve CAPTCHA:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$captchaNum1 + $captchaNum2 = ?",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            TextField(
                                value = captchaAnswerInput,
                                onValueChange = { captchaAnswerInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Result") },
                                modifier = Modifier.weight(1f).testTag("captcha_input")
                            )
                        }
                    }
                }

                // OTP Block (only required if free voting, to replicate real-time phone fraud detection)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("2. Phone OTP Validation:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!isOtpSent) {
                            Button(
                                onClick = {
                                    generatedOtp = (1000..9999).random().toString()
                                    isOtpSent = true
                                    errorText = "SIMULATED SMS: Your ATM VOTE code is: $generatedOtp"
                                },
                                modifier = Modifier.fillMaxWidth().testTag("send_otp_button")
                            ) {
                                Text("Send Phone Verification OTP")
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextField(
                                    value = userOtpInput,
                                    onValueChange = { userOtpInput = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    label = { Text("4-digit Code") },
                                    modifier = Modifier.weight(1f).testTag("otp_input")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resend",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        generatedOtp = (1000..9999).random().toString()
                                        errorText = "SIMULATED SMS: Your NEW ATM VOTE code is: $generatedOtp"
                                    }
                                )
                            }
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText,
                        color = if (errorText.startsWith("SIMULATED")) MintGreen else MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mathAnswer = captchaNum1 + captchaNum2
                    if (captchaAnswerInput.toIntOrNull() != mathAnswer) {
                        errorText = "Incorrect CAPTCHA answer. Try again."
                        return@Button
                    }
                    if (isOtpSent && userOtpInput != generatedOtp) {
                        errorText = "Invalid Phone OTP. Check the SMS notification simulation above."
                        return@Button
                    }
                    onVerified()
                },
                modifier = Modifier.testTag("verify_confirm_button")
            ) {
                Text("Verify & Proceed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PaymentSimulationModal(
    packageItem: VotePackage,
    paymentMethod: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onPaymentSuccess: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Input details, 2: Simulating processing, 3: Success
    var phoneNumber by remember { mutableStateOf("+233 ") }
    var pinNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var simulatedSmsOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val discountAmount = if (promoCode.uppercase() == "ATM10") packageItem.priceGHS * 0.1 else 0.0
    val finalPrice = packageItem.priceGHS - discountAmount

    val event = viewModel.selectedEvent.collectAsState().value
    var splitResult by remember { mutableStateOf<com.example.data.service.PaymentIntegrationService.PaymentSplitResult?>(null) }

    LaunchedEffect(paymentMethod, finalPrice, event) {
        if (event != null && (paymentMethod == "PAYSTACK" || paymentMethod == "FLUTTERWAVE")) {
            splitResult = viewModel.paymentService.calculateSplit(finalPrice, event.organizationId)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        paymentMethod.contains("MOMO") -> Icons.Default.PhoneAndroid
                        paymentMethod.contains("STRIPE") -> Icons.Default.CreditCard
                        else -> Icons.Default.AccountBalanceWallet
                    },
                    contentDescription = "Payment",
                    tint = MintGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$paymentMethod Secure Gateway", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (step == 1) {
                    Text(
                        text = "Total votes: ${packageItem.votes} | Amount: GHS ${String.format("%.2f", finalPrice)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (paymentMethod.contains("MONEY") || paymentMethod.contains("MOMO")) {
                        Text("MTN Mobile Money / Telecel Cash / AirtelTigo Ghana payment simulation", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Mobile Money Number") },
                            modifier = Modifier.fillMaxWidth().testTag("payment_phone_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = pinNumber,
                            onValueChange = { pinNumber = it },
                            label = { Text("Enter Momo Wallet PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth().testTag("payment_pin_input")
                        )
                    } else {
                        Text("$paymentMethod payment service simulation gateway", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("16-digit Card Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("payment_card_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("Cardholder Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("payment_cardname_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = promoCode,
                        onValueChange = { promoCode = it },
                        label = { Text("Enter Promo Code (Use 'ATM10' for 10% off)") },
                        modifier = Modifier.fillMaxWidth().testTag("payment_promo_input")
                    )

                    if (paymentMethod == "PAYSTACK" || paymentMethod == "FLUTTERWAVE") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Split info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$paymentMethod SPLIT BILLING SIMULATION",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                splitResult?.let { split ->
                                    val organizerRatio = 100.0 - split.commissionRate
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Commission Rate:", fontSize = 11.sp, color = Color.Gray)
                                        Text("${split.commissionRate}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Platform Fee (Super Admin):", fontSize = 11.sp, color = Color.Gray)
                                        Text("GHS ${String.format("%.2f", split.commissionAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Organizer Net Revenue:", fontSize = 11.sp, color = Color.Gray)
                                        Text("GHS ${String.format("%.2f", split.netAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Organizer Subaccount:", fontSize = 11.sp, color = Color.Gray)
                                        Text(split.organizerSubaccount, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    var showPayload by remember { mutableStateOf(false) }
                                    TextButton(
                                        onClick = { showPayload = !showPayload },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(if (showPayload) "Hide Gateway JSON Payload" else "Show Gateway JSON Payload", fontSize = 11.sp)
                                    }

                                    if (showPayload) {
                                        val payload = if (paymentMethod == "PAYSTACK") {
                                            """
                                            {
                                              "email": "${viewModel.currentUser.value?.email ?: "voter@atmvote.com"}",
                                              "amount": ${(finalPrice * 100).toLong()},
                                              "currency": "GHS",
                                              "reference": "PSTK_${UUID.randomUUID().toString().take(8).uppercase()}",
                                              "split": {
                                                "name": "Dynamic Revenue Split",
                                                "type": "percentage",
                                                "subaccounts": [
                                                  {
                                                    "subaccount": "${split.organizerSubaccount}",
                                                    "share": $organizerRatio
                                                  }
                                                ]
                                              }
                                            }
                                            """.trimIndent()
                                        } else {
                                            """
                                            {
                                              "tx_ref": "FLW_${UUID.randomUUID().toString().take(8).uppercase()}",
                                              "amount": ${String.format("%.2f", finalPrice)},
                                              "currency": "GHS",
                                              "customer": {
                                                "email": "${viewModel.currentUser.value?.email ?: "voter@atmvote.com"}"
                                              },
                                              "subaccounts": [
                                                {
                                                  "id": "RS_${split.organizerSubaccount.removePrefix("ACCT_")}",
                                                  "transaction_split_ratio": $organizerRatio,
                                                  "transaction_charge_type": "percentage"
                                                }
                                              ]
                                            }
                                            """.trimIndent()
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = payload,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (errorMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                } else if (step == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MintGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Simulating payment callback webhook standard...", textAlign = TextAlign.Center, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (simulatedSmsOtp.isEmpty()) {
                            LaunchedEffect(Unit) {
                                delay(1200)
                                simulatedSmsOtp = (100000..999999).random().toString()
                            }
                        } else {
                            Text(
                                "SIMULATED BANK SMS: Code is: $simulatedSmsOtp",
                                color = MintGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = enteredOtp,
                                onValueChange = { enteredOtp = it },
                                label = { Text("Enter Bank SMS Code") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = {
                        if (paymentMethod.contains("MOMO") && (phoneNumber.length < 9 || pinNumber.isBlank())) {
                            errorMsg = "Please input valid phone and Momo PIN"
                            return@Button
                        }
                        if (!paymentMethod.contains("MOMO") && (cardNumber.length < 12 || cardName.isBlank())) {
                            errorMsg = "Please input credit card details"
                            return@Button
                        }
                        step = 2
                    },
                    modifier = Modifier.testTag("payment_pay_now_button")
                ) {
                    Text("Pay GHS ${String.format("%.2f", finalPrice)}")
                }
            } else if (step == 2) {
                Button(
                    onClick = {
                        if (enteredOtp == simulatedSmsOtp) {
                            transactionId = "TXN_${paymentMethod}_" + UUID.randomUUID().toString().take(10).uppercase()
                            onPaymentSuccess(promoCode)
                        } else {
                            errorMsg = "Invalid OTP"
                        }
                    },
                    enabled = enteredOtp.isNotEmpty()
                ) {
                    Text("Confirm OTP & Cast Votes")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InteractiveReceiptCard(
    transaction: TransactionEntity,
    votesCast: Int,
    contestantNumber: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Receipt Generated!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MintGreen)
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MintGreen, modifier = Modifier.size(28.dp))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "ATM VOTE SECURE RECEIPT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                ReceiptRow(label = "Platform", value = "ATM VOTE SaaS")
                ReceiptRow(label = "TX Hash", value = transaction.id.take(12).uppercase())
                ReceiptRow(label = "Event", value = transaction.eventTitle)
                ReceiptRow(label = "Contestant", value = transaction.contestantName)
                ReceiptRow(label = "Number", value = contestantNumber)
                ReceiptRow(label = "Votes Added", value = "+$votesCast Votes")
                ReceiptRow(label = "Paid By", value = transaction.voterName)
                ReceiptRow(label = "Date/Time", value = sdf.format(Date(transaction.timestamp)))
                ReceiptRow(label = "Method", value = transaction.paymentMethod)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                ReceiptRow(label = "Gross Amount", value = "GHS ${String.format("%.2f", transaction.amount)}")
                ReceiptRow(
                    label = "Platform Comm.",
                    value = "GHS ${String.format("%.2f", transaction.commissionAmount)}",
                    textColor = MaterialTheme.colorScheme.error
                )
                ReceiptRow(
                    label = "Org Net Earnings",
                    value = "GHS ${String.format("%.2f", transaction.netAmount)}",
                    textColor = MintGreen,
                    isBold = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This receipt is digitally signed and encrypted to prevent vote fraud or audit tampering. Supported by Paystack, Stripe, and ATM Generations Group.",
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = """
                        🏆 ATM VOTE Confirmed!
                        I successfully voted $votesCast times for ${transaction.contestantName} ($contestantNumber) in the ${transaction.eventTitle}!
                        
                        🔒 Secure TX ID: ${transaction.id}
                        ⚡ Cast yours instantly via MTN MoMo, Telecel Cash, or Card!
                        📍 Location: Sunyani, Bono Region, Ghana
                        📞 Support: +233 546 541 560
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share ATM Vote Receipt"))
                },
                modifier = Modifier.testTag("receipt_share_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("receipt_close_button")) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ReceiptRow(label: String, value: String, textColor: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 160.dp)
        )
    }
}

@Composable
fun MockFileExporter(
    title: String,
    columns: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Analytics Reporting", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Export dynamic ledgers to PDF, Excel, and CSV", fontSize = 10.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Assessment, contentDescription = "Report", tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ExportButton(
                label = "CSV",
                icon = Icons.Default.InsertDriveFile,
                onClick = {
                    isExporting = true
                    val csvString = buildString {
                        append(columns.joinToString(",")).append("\n")
                        for (row in rows) {
                            append(row.joinToString(",")).append("\n")
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csvString)
                        type = "text/comma-separated-values"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export ATM VOTE CSV"))
                    isExporting = false
                }
            )

            ExportButton(
                label = "Excel",
                icon = Icons.Default.TableChart,
                onClick = {
                    val xmlXlsString = buildString {
                        append("ATM VOTE EXCEL DUMP\n")
                        append("Title: $title\n\n")
                        append(columns.joinToString("\t")).append("\n")
                        for (row in rows) {
                            append(row.joinToString("\t")).append("\n")
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, xmlXlsString)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export ATM VOTE XLS"))
                }
            )

            ExportButton(
                label = "PDF",
                icon = Icons.Default.PictureAsPdf,
                onClick = {
                    val pdfSimString = buildString {
                        append("ATM VOTE DIGITAL SYSTEM REPORT\n")
                        append("===============================\n")
                        append("REPORT TITLE: $title\n")
                        append("DATE GENERATED: ${Date()}\n")
                        append("===============================\n\n")
                        for (row in rows) {
                            append(row.joinToString(" | ")).append("\n")
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, pdfSimString)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export ATM VOTE PDF"))
                }
            )
        }
    }
}

@Composable
fun ExportButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
