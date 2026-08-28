package com.nanokernel.expensetracker.data.repository

import com.nanokernel.expensetracker.data.local.EventDao
import com.nanokernel.expensetracker.data.local.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val dao: EventDao) {

    val allEvents: Flow<List<EventEntity>> = dao.getAllEvents()

    suspend fun addEvent(name: String, emoji: String, budget: Double?, createdDateMillis: Long) {
        dao.insert(
            EventEntity(
                name = name.trim(),
                emoji = emoji.trim().ifBlank { "🎉" },
                budget = budget,
                createdDateMillis = createdDateMillis
            )
        )
    }

    suspend fun updateEvent(original: EventEntity, name: String, emoji: String, budget: Double?) {
        dao.update(original.copy(name = name.trim(), emoji = emoji.trim().ifBlank { "🎉" }, budget = budget))
    }

    suspend fun setArchived(event: EventEntity, archived: Boolean) {
        dao.update(event.copy(isArchived = archived))
    }

    suspend fun deleteEvent(event: EventEntity) = dao.delete(event)
}
