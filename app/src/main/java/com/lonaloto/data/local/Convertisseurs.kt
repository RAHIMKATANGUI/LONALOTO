package com.lonaloto.data.local

import androidx.room.TypeConverter
import com.lonaloto.data.local.entities.Role
import com.lonaloto.data.local.entities.TypeAction
import java.util.Date

class Convertisseurs {

    @TypeConverter
    fun depuisTimestamp(valeur: Long?): Date? = valeur?.let { Date(it) }

    @TypeConverter
    fun versTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun depuisRole(valeur: String?): Role? = valeur?.let { Role.valueOf(it) }

    @TypeConverter
    fun versRole(role: Role?): String? = role?.name

    @TypeConverter
    fun depuisTypeAction(valeur: String?): TypeAction? = valeur?.let { TypeAction.valueOf(it) }

    @TypeConverter
    fun versTypeAction(action: TypeAction?): String? = action?.name
}
