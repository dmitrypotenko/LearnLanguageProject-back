package com.dpotenko.kirillweb.converter

import org.jooq.Converter

class BooleanConverter : Converter<Byte, Boolean> {
    override fun from(databaseObject: Byte?): Boolean {
        if (databaseObject != null && databaseObject.toInt() == 1) {
            return true
        }
        return false
    }

    override fun to(userObject: Boolean?): Byte {
        if (userObject != null && userObject) {
            return 1;
        } else {
            return 0
        }
    }

    override fun fromType(): Class<Byte> {
        return Byte::class.java
    }

    override fun toType(): Class<Boolean> {
        return Boolean::class.java
    }
}