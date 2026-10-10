package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun BrandProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    var showAddSocialDialog by remember { mutableStateOf(false) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showEditBrandDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Brand Profile & Whitelist",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Define verified legitimate corporate assets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberCyan.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCyan))
                ) {
                    Text(
                        text = "VERIFIED TENANT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Critical Rule Highlight Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SafeGreenBg.copy(alpha = 0.25f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeGreenText))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Exclusion Rule Active",
                        tint = SafeGreenText,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Critical Policy: Strict Official Asset Exclusion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreenText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All verified official social handles, URLs, and mobile application packages configured below are cryptographically whitelisted and guaranteed never to trigger threat alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Preset Switcher
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Active Enterprise Tenant",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            "APEX" to "Apex Financial",
                            "CLOUD_VAULT" to "CloudVault",
                            "AEROPAY" to "AeroPay"
                        )
                        presets.forEach { (id, name) ->
                            val isSelected = currentProfile.name.contains(name.take(4), ignoreCase = true)
                            FilledTonalButton(
                                onClick = { viewModel.switchBrandProfile(id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preset_tenant_$id"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) CyberCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(name, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Brand Identity Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Corporate Brand Identity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilledTonalButton(
                            onClick = { showEditBrandDialog = true },
                            modifier = Modifier.testTag("edit_brand_identity_button"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Brand & Logo", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberNavy)
                                .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Logo",
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = currentProfile.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Logo spec: ${currentProfile.logoDescription}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentProfile.aliases.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Protected Brand Stems & Aliases:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentProfile.aliases.forEach { alias ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = alias,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Official Social Accounts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Official Social Accounts (${currentProfile.officialSocialAccounts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { showAddSocialDialog = true },
                    modifier = Modifier.testTag("add_social_account_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Account", fontSize = 12.sp)
                }
            }
        }

        items(currentProfile.officialSocialAccounts) { social ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = CyberBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = social.handle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SafeGreenText, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "${social.platform.displayName} • ${social.profileUrl}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SafeGreenBg
                    ) {
                        Text(
                            text = "WHITELISTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreenText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Official Mobile Applications Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Official Mobile Applications (${currentProfile.officialMobileApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { showAddAppDialog = true },
                    modifier = Modifier.testTag("add_mobile_app_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App", fontSize = 12.sp)
                }
            }
        }

        items(currentProfile.officialMobileApps) { app ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = app.appTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Package: ${app.packageName}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan
                            )
                            Text(
                                text = "Official Publisher: ${app.developerName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SafeGreenBg
                    ) {
                        Text(
                            text = "WHITELISTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreenText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // Add Social Dialog
    if (showAddSocialDialog) {
        var handle by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf(PlatformType.TWITTER) }

        AlertDialog(
            onDismissRequest = { showAddSocialDialog = false },
            title = { Text("Add Official Social Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This account will be whitelisted and excluded from all threat alerts.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it },
                        label = { Text("Handle (e.g. @MyBrand)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Profile URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (handle.isNotBlank()) {
                            viewModel.addOfficialSocial(platform, handle, url)
                            showAddSocialDialog = false
                        }
                    }
                ) {
                    Text("Add & Whitelist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSocialDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add App Dialog
    if (showAddAppDialog) {
        var appTitle by remember { mutableStateOf("") }
        var packageName by remember { mutableStateOf("") }
        var devName by remember { mutableStateOf(currentProfile.name) }

        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("Add Official Mobile Application") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This package ID and publisher will be immune from impersonation detection.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = appTitle,
                        onValueChange = { appTitle = it },
                        label = { Text("App Title (e.g. Acme Mobile Banking)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("Package ID (e.g. com.acme.banking)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = devName,
                        onValueChange = { devName = it },
                        label = { Text("Developer / Publisher Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (packageName.isNotBlank()) {
                            viewModel.addOfficialApp(AppStoreType.GOOGLE_PLAY, packageName, appTitle, devName)
                            showAddAppDialog = false
                        }
                    }
                ) {
                    Text("Add & Whitelist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Brand Identity & Logo Spec Dialog
    if (showEditBrandDialog) {
        var brandName by remember { mutableStateOf(currentProfile.name) }
        var aliasesText by remember { mutableStateOf(currentProfile.aliases.joinToString(", ")) }
        var logoDescription by remember { mutableStateOf(currentProfile.logoDescription) }

        AlertDialog(
            onDismissRequest = { showEditBrandDialog = false },
            title = { Text("Configure Brand Profile & Logo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Define your legitimate organization's brand identity. All detection algorithms reference these values to identify look-alikes and logo clones.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Company / Brand Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = aliasesText,
                        onValueChange = { aliasesText = it },
                        label = { Text("Protected Aliases (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. ApexBank, ApexFin, ApexPay") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = logoDescription,
                        onValueChange = { logoDescription = it },
                        label = { Text("Official Logo Specification") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Blue gradient shield with cyan radar reticle") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (brandName.isNotBlank()) {
                            val parsedAliases = aliasesText.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            viewModel.updateCustomBrand(
                                name = brandName.trim(),
                                aliases = parsedAliases,
                                logoDesc = logoDescription.trim()
                            )
                            showEditBrandDialog = false
                        }
                    }
                ) {
                    Text("Save & Update Engine")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBrandDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
