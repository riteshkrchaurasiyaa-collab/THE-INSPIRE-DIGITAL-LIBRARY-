package com.example.data

data class LibraryPlan(
    val slNo: Int,
    val category: String, // "FOUR HOURS SHIFTS", "SIX HOURS SHIFTS", "EIGHT HOURS SHIFTS", "TWELVE, TWENTY FOUR AND ONLY NIGHT SHIFT"
    val shiftName: String, // e.g. "FIRST SHIFT", "HALF DAY SHIFT", "FULL DAY SHIFT", "ONLY NIGHT SHIFT"
    val timings: String, // e.g. "6AM - 10AM"
    val price: Double, // e.g. 300.0
    val durationHours: Int // 4, 6, 8, 12, 24, 11
) {
    val displayName: String
        get() = "$shiftName ($timings) - ₹${price.toInt()}/-"
}

object LibraryRateList {
    val PLANS = listOf(
        // FOUR HOURS SHIFTS (₹300/-)
        LibraryPlan(1, "FOUR HOURS SHIFTS", "FIRST SHIFT", "6AM - 10AM", 300.0, 4),
        LibraryPlan(2, "FOUR HOURS SHIFTS", "SECOND SHIFT", "10AM - 2PM", 300.0, 4),
        LibraryPlan(3, "FOUR HOURS SHIFTS", "THIRD SHIFT", "2PM - 6PM", 300.0, 4),
        LibraryPlan(4, "FOUR HOURS SHIFTS", "FOURTH SHIFT", "6PM - 10PM", 300.0, 4),

        // SIX HOURS SHIFTS (₹450/-)
        LibraryPlan(5, "SIX HOURS SHIFTS", "FIRST SHIFT", "7AM - 1PM", 450.0, 6),
        LibraryPlan(6, "SIX HOURS SHIFTS", "SECOND SHIFT", "1PM - 7PM", 450.0, 6),

        // EIGHT HOURS SHIFTS (₹550/-)
        LibraryPlan(7, "EIGHT HOURS SHIFTS", "FIRST SHIFT", "6AM - 2PM", 550.0, 8),
        LibraryPlan(8, "EIGHT HOURS SHIFTS", "SECOND SHIFT", "2PM - 10PM", 550.0, 8),

        // TWELVE, TWENTY FOUR AND ONLY NIGHT SHIFT
        LibraryPlan(9, "TWELVE, TWENTY FOUR AND ONLY NIGHT SHIFT", "HALF DAY SHIFT", "6AM - 6PM", 700.0, 12),
        LibraryPlan(10, "TWELVE, TWENTY FOUR AND ONLY NIGHT SHIFT", "FULL DAY SHIFT", "6AM - 6AM", 800.0, 24),
        LibraryPlan(11, "TWELVE, TWENTY FOUR AND ONLY NIGHT SHIFT", "ONLY NIGHT SHIFT", "7PM - 6AM", 400.0, 11)
    )

    fun findPlanByShiftName(shift: String): LibraryPlan? {
        val clean = shift.trim().lowercase()
        return PLANS.find { p ->
            clean.contains(p.timings.lowercase()) ||
            clean.contains(p.shiftName.lowercase()) ||
            (clean.contains("night") && p.shiftName.contains("NIGHT", ignoreCase = true)) ||
            (clean.contains("half") && p.shiftName.contains("HALF", ignoreCase = true)) ||
            (clean.contains("full") && p.shiftName.contains("FULL", ignoreCase = true))
        } ?: PLANS.find { p -> clean.contains("${p.price.toInt()}") }
    }
}
