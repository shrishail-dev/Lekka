package com.nanokernel.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// A classic "ledger" palette: deep forest green + warm brass, on a warm cream page —
// evokes a paper account book rather than a generic app.
val Primary = Color(0xFF1F5D42)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFCFE8D8)
val OnPrimaryContainer = Color(0xFF0A2E1D)

val Secondary = Color(0xFFB4772A)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF6DFBC)
val OnSecondaryContainer = Color(0xFF422C06)

val Tertiary = Color(0xFF6B4A8A)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFE8DCF2)

val Background = Color(0xFFFAF6EE)
val OnBackground = Color(0xFF241F16)
val Surface = Color(0xFFFFFDF8)
val OnSurface = Color(0xFF241F16)
val SurfaceVariant = Color(0xFFEDE6D6)
val OnSurfaceVariant = Color(0xFF544C3D)
val Outline = Color(0xFFC9BFA8)

// A clean blue accent reserved for the Monthly Report screen (totals, bar chart) — keeps
// the ledger green/gold identity elsewhere while giving reports their own recognizable look.
val ReportAccent = Color(0xFF2F6FED)
val ReportAccentContainer = Color(0xFFDCE6FB)
val ReportAccentMuted = Color(0xFFAFC3EE)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

// Dark theme: same warm identity, deepened rather than swapped to a cold neutral gray.
val PrimaryDark = Color(0xFF8FD4AE)
val OnPrimaryDark = Color(0xFF073820)
val PrimaryContainerDark = Color(0xFF0F4530)
val OnPrimaryContainerDark = Color(0xFFCFE8D8)

val SecondaryDark = Color(0xFFE7B871)
val OnSecondaryDark = Color(0xFF432D04)
val SecondaryContainerDark = Color(0xFF614109)
val OnSecondaryContainerDark = Color(0xFFF6DFBC)

val BackgroundDark = Color(0xFF1B1712)
val OnBackgroundDark = Color(0xFFEAE2D4)
val SurfaceDark = Color(0xFF231F18)
val OnSurfaceDark = Color(0xFFEAE2D4)
val SurfaceVariantDark = Color(0xFF3C362B)
val OnSurfaceVariantDark = Color(0xFFD0C6B1)

// Fixed palette so a category's color stays consistent across the donut chart, legend,
// and bars. Muted, ink-and-paper tones rather than saturated "app" colors.
val CategoryColors = listOf(
    Color(0xFFC1543B), // Food
    Color(0xFF3F6FA8), // Transport
    Color(0xFF3F8F62), // Groceries
    Color(0xFFC99A2E), // Bills
    Color(0xFF7C5A9E), // Fun
    Color(0xFFB6567F), // Shopping
    Color(0xFF4E8E8E), // extra (custom categories)
    Color(0xFF8C7A4E)  // extra (custom categories)
)
