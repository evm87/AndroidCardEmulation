package com.evanmeyermann.cardemulationexample.storage

import io.realm.Realm
import java.util.concurrent.Callable

// Thrown to specify that a realm transaction failed
class RepositoryException(message: String) : Exception(message)

/**
 * Provides realm helper functions
 */
object RealmHelper {
    /**
     * Executes the passed transaction on the passed realm
     * If the transaction fails for some reason, it rolls back the transaction and throws
     * Finally, the realm is always closed.
     */
    fun <T> executeTransaction(realm: Realm, transaction: Callable<T>) : T {
        realm.beginTransaction()
        return try {
            val result = transaction.call()
            realm.commitTransaction()
            result
        } catch (ex: Exception) {
            if (realm.isInTransaction) {
                realm.cancelTransaction()
            }
            throw RepositoryException(ex.message ?: "Realm transaction failed.")
        }
    }
}