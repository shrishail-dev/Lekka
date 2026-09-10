package com.nanokernel.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// A clean modern "fintech" palette — vivid emerald for money/growth, warm coral as the single
// accent for primary actions (the Add button, key CTAs), true neutral surfaces rather than a
// tinted "paper" look, so both light and dark mode read as crisp and current.
val Primary = Color(0xFF0E9F6E)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD3F5E6)
val OnPrimaryContainer = Color(0xFF00391F)

val Secondary = Color(0xFFEA6C3D)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFFDACB)
val OnSecondaryContainer = Color(0xFF4A1B03)

val Tertiary = Color(0xFF5B5FEF)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFE1E0FF)

val Background = Color(0xFFF7F8F7)
val OnBackground = Color(0xFF1A1C1A)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1A1C1A)
val SurfaceVariant = Color(0xFFEBEDEA)
val OnSurfaceVariant = Color(0xFF565C57)
val Outline = Color(0xFFDADFDA)

// A clean blue accent reserved for the Monthly Report screen (totals, bar chart) — keeps the
// emerald/coral identity elsewhere while giving reports their own recognizable look.
val ReportAccent = Color(0xFF2F6FED)
val ReportAccentContainer = Color(0xFFDCE6FB)
val ReportAccentMuted = Color(0xFFAFC3EE)

val Error = Color(0xFFDC2626)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// Dark theme: true neutral dark (not brown-tinted) with a brighter emerald for legibility.
val PrimaryDark = Color(0xFF5CE0A8)
val OnPrimaryDark = Color(0xFF00391F)
val PrimaryContainerDark = Color(0xFF0A5A38)
val OnPrimaryContainerDark = Color(0xFFD3F5E6)

val SecondaryDark = Color(0xFFFF9868)
val OnSecondaryDark = Color(0xFF4A1B03)
val SecondaryContainerDark = Color(0xFF7A3413)
val OnSecondaryContainerDark = Color(0xFFFFDACB)

val BackgroundDark = Color(0xFF121412)
val OnBackgroundDark = Color(0xFFE3E5E2)
val SurfaceDark = Color(0xFF1C1F1C)
val OnSurfaceDark = Color(0xFFE3E5E2)
val SurfaceVariantDark = Color(0xFF2B2F2B)
val OnSurfaceVariantDark = Color(0xFFC2C8C2)

// Fixed palette so a category's color stays consistent across the donut chart, legend, and
// bars — vivid but not neon, distinct enough at a glance across 6-8 categories.
val CategoryColors = listOf(
    Color(0xFFEF5350), // Food
    Color(0xFF42A5F5), // Transport
    Color(0xFF26A69A), // Groceries
    Color(0xFFFFA726), // Bills
    Color(0xFFAB47BC), // Fun
    Color(0xFFEC407A), // Shopping
    Color(0xFF7E57C2), // extra (custom categories)
    Color(0xFF8D6E63)  // extra (custom categories)
)
