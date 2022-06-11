package me.ash.reader.data.model.account

import androidx.room.TypeConverter

class AccountType(val id: Int) {

    init {
        if (id < 1 || id > 3) {
            throw IllegalArgumentException("Account type id is not valid.")
        }
    }

    companion object {

        val Local = AccountType(1)
        val Fever = AccountType(2)
        val GoogleReader = AccountType(3)
    }
}

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
