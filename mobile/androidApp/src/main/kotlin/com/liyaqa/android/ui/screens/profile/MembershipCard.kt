package com.liyaqa.android.ui.screens.profile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.liyaqa.android.ui.theme.LiyaqaBrand
import com.liyaqa.android.ui.theme.LiyaqaBrandVariant
import com.liyaqa.android.ui.theme.Warning
import com.liyaqa.gym.domain.Subscription
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

/**
 * Membership Card composable
 * Displays digital membership card with plan info, expiry date, and QR code
 */
@Composable
fun MembershipCard(
    subscription: Subscription?,
    memberId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (subscription == null) {
        // No active subscription
        NoSubscriptionCard(
            onClick = onClick,
            modifier = modifier
        )
    } else {
        // Active subscription card
        ActiveMembershipCard(
            subscription = subscription,
            memberId = memberId,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
private fun NoSubscriptionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CardMembership,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Membership",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Subscribe to a plan to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiyaqaBrand
                    )
                ) {
                    Text("View Plans")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveMembershipCard(
    subscription: Subscription,
    memberId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val daysRemaining = subscription.daysRemaining(currentDate) ?: Int.MAX_VALUE
    val isExpiringSoon = daysRemaining in 1..30
    val isExpired = daysRemaining == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(LiyaqaBrandVariant, LiyaqaBrand)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subscription.planName ?: "Membership",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (subscription.isVisitBased() && subscription.remainingVisits != null) {
                            Text(
                                text = "${subscription.remainingVisits} visits remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.CardMembership,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Expiry info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isExpired) "Expired" else "Valid until",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = subscription.endDate?.toString() ?: "Unlimited",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Warning for expiring soon
                if (isExpiringSoon && !isExpired) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Expires in $daysRemaining days - Renew now",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // QR Code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QRCode(
                        content = memberId,
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Member ID
                Text(
                    text = "Member ID: ${memberId.take(8).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * QR Code generator composable
 */
@Composable
private fun QRCode(
    content: String,
    modifier: Modifier = Modifier
) {
    val qrCodeBitmap = remember(content) {
        generateQRCode(content, 400, 400)
    }

    qrCodeBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code for $content",
            modifier = modifier
        )
    } ?: run {
        // Fallback if QR code generation fails
        Box(
            modifier = modifier.background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QR Code",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Generate QR code bitmap
 */
private fun generateQRCode(content: String, width: Int, height: Int): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height
        )

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
