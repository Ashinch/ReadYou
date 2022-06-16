package me.ash.reader.data.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter

/**
 * Each account will specify its local or third-party API type.
 */
class AccountType(val id: Int) {

    /**
     * Make sure the constructed object is valid.
     */
    init {
        if (id < 1 || id > 3) {
            throw IllegalArgumentException("Account type id is not valid.")
        }
    }

    /**
     * Type of account currently supported.
     */
    companion object {

        val Local = AccountType(1)
        val Fever = AccountType(2)
        val GoogleReader = AccountType(3)
    }
}

/**
 * Provide [TypeConverter] of [AccountType] for [RoomDatabase].
 */
class AccountTypeConverters {

    @TypeConverter
    fun toAccountType(id: Int): AccountType {
        return AccountType(id)
    }

    @TypeConverter
    fun fromAccountType(accountType: AccountType): Int {
        return accountType.id
    }
}
