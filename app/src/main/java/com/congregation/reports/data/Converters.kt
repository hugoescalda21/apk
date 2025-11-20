package com.congregation.reports.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPublisherType(value: PublisherType): String {
        return value.name
    }

    @TypeConverter
    fun toPublisherType(value: String): PublisherType {
        return PublisherType.valueOf(value)
    }

    @TypeConverter
    fun fromMeetingType(value: MeetingType): String {
        return value.name
    }

    @TypeConverter
    fun toMeetingType(value: String): MeetingType {
        return MeetingType.valueOf(value)
    }
}
