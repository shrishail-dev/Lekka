package com.nanokernel.expensetracker.reminder

/** Rotated randomly for the daily "log your expenses" nudge — kept light so it doesn't feel like nagging. */
val reminderMessages = listOf(
    "Your wallet called. It misses you. Log today's spending! 💸",
    "Somewhere, a receipt is crying because nobody logged it yet. 🧾😭",
    "Plot twist: your money didn't vanish, you just forgot to write it down.",
    "Ding dong! Time for your daily \"did I actually spend money today\" check-in.",
    "Your future self is quietly judging you for skipping today's expenses. 👀",
    "Breaking news: expenses still don't log themselves. Do the honors?",
    "Tick tock — another chance to admit you bought that snack. Log it anyway.",
    "Your budget is doing fine... probably. Log today's expenses to be sure. 😅",
    "Rumor has it you spent money today. Confirm or deny in Lekka!",
    "This is your wallet, filing a missing-expense report. Open Lekka to close the case.",
    "Two minutes now saves you a mystery later: \"Where did that money go?\"",
    "Your money left the building today. Log where it went before it forgets too."
)

fun randomReminderMessage(): String = reminderMessages.random()
