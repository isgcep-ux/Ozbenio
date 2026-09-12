package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CastDao {
    @Query("SELECT * FROM cast_sessions ORDER BY timestamp DESC LIMIT 20")
    fun getAllSessions(): Flow<List<CastSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CastSessionEntity)

    @Query("DELETE FROM cast_sessions")
    suspend fun clearAllSessions()

    @Query("SELECT * FROM custom_streams ORDER BY createdAt DESC")
    fun getAllCustomStreams(): Flow<List<CustomStreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomStream(stream: CustomStreamEntity)

    @Query("DELETE FROM custom_streams WHERE id = :streamId")
    suspend fun deleteCustomStream(streamId: String)

    // --- Playlists ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    // --- Playlist Items ---
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC, addedAt ASC")
    fun getPlaylistItems(playlistId: String): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC, addedAt ASC")
    suspend fun getPlaylistItemsSync(playlistId: String): List<PlaylistItemEntity>

    @Query("SELECT * FROM playlist_items")
    fun getAllPlaylistItems(): Flow<List<PlaylistItemEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getPlaylistItemCount(playlistId: String): Int

    @Query("SELECT MAX(orderIndex) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getMaxOrderIndex(playlistId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItems(items: List<PlaylistItemEntity>)

    @Update
    suspend fun updatePlaylistItems(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlist_items WHERE id = :itemId")
    suspend fun deletePlaylistItemById(itemId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun deletePlaylistItem(playlistId: String, mediaId: String)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deleteAllItemsInPlaylist(playlistId: String)

    @Transaction
    suspend fun deletePlaylistWithItems(playlistId: String) {
        deleteAllItemsInPlaylist(playlistId)
        deletePlaylist(playlistId)
    }

    @Transaction
    suspend fun reorderItemsInPlaylist(playlistId: String, updatedItems: List<PlaylistItemEntity>) {
        updatePlaylistItems(updatedItems)
    }

    // --- Recent Connected Devices (Last 5 TVs) ---
    @Query("SELECT * FROM recent_devices ORDER BY lastConnectedTimestamp DESC LIMIT 5")
    fun getRecentDevices(): Flow<List<RecentDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecentDevice(device: RecentDeviceEntity)

    @Query("DELETE FROM recent_devices WHERE id = :deviceId")
    suspend fun deleteRecentDevice(deviceId: String)

    @Query("DELETE FROM recent_devices")
    suspend fun clearRecentDevices()
}

