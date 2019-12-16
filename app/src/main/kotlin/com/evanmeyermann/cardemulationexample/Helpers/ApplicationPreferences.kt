package com.evanmeyermann.cardemulationexample.helpers

import android.content.SharedPreferences
import android.util.Base64
import java.io.*
import java.util.concurrent.atomic.AtomicReference

@Suppress("unused")
/**
 * Class for managing application preferences
 */
private const val TAG = "ApplicationPreferences"

class ApplicationPreferences(private val prefs: SharedPreferences) {

    private val editorAtomicReference = AtomicReference<SharedPreferences.Editor>()

    /**
     * Return preferences string associated with the key
     */
    fun getString(key: String): String = prefs.getString(key, "")

    fun getString(key: String, defValue: String): String = prefs.getString(key, defValue)

    /**
     * Set key to a specified value
     */
    fun setString(key: String, value: String): ApplicationPreferences {
        editor.putString(key, value)
        return this
    }

    /**
     * Return editor for manipulating preferences
     */
    private val editor: SharedPreferences.Editor
        get() {
            var editor = editorAtomicReference.get()
            if (editor == null) {
                editor = prefs.edit()
                if (!editorAtomicReference.compareAndSet(null, editor)) {
                    editor = editorAtomicReference.get()
                }
            }
            return editor
        }

    /**
     * Call to apply any preferences changes.
     */
    fun apply() {
        editorAtomicReference.getAndSet(null)?.apply()
    }

    /**
     * Clears all entries from preferences
     */
    fun clear() {
        editor.clear()
    }

    /**
     * Return true if the key exists in the preferences
     */
    operator fun contains(key: String): Boolean = prefs.contains(key)

    /**
     * Return true if the boolean key is set to true
     */
    fun getBoolean(key: String): Boolean = prefs.getBoolean(key, false)

    /**
     * Return boolean value associated with the key.defValue is returned if the
     * key doesn't exist.
     */
    fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)

    /**
     * Set boolean key
     */
    fun setBoolean(key: String, value: Boolean): ApplicationPreferences {
        editor.putBoolean(key, value)
        return this
    }

    /**
     * Return long value associated with the key.defValue is returned if the key
     * doesn't exist.
     */
    fun getLong(key: String, defValue: Long): Long = prefs.getLong(key, defValue)

    /**
     * Set long key
     */
    fun setLong(key: String, value: Long): ApplicationPreferences {
        editor.putLong(key, value)
        return this
    }

    /**
     * Return int value associated with the key. defValue is returned if the key
     * doesn't exist.
     */
    fun getInt(key: String, defValue: Int): Int = prefs.getInt(key, defValue)

    /**
     * Set int key
     */
    fun setInt(key: String, value: Int): ApplicationPreferences {
        editor.putInt(key, value)
        return this
    }

    /**
     * Used to remove setting with the specified key
     */
    fun remove(key: String): ApplicationPreferences {
        editor.remove(key)
        return this
    }

    /**
     * Used to save serializable object in preferences.
     */
    fun setSerializable(key: String, value: Serializable): ApplicationPreferences {
        try {
            val byteStream = ByteArrayOutputStream(1024)
            val oStream = ObjectOutputStream(byteStream)
            oStream.writeObject(value)

            //Now encode the data
            editor.putString(key, String(Base64.encode(byteStream.toByteArray(),
                    Base64.DEFAULT)))
        } catch (error: IOException) {
            //Log.d(TAG, "Failed to serialise object for key: $key", error)
        }

        return this
    }

    /**
     * Used to retrieve serializable object previously stored in preferences
     */
    fun <T : Serializable> getSerializable(key: String): T? {
        val storedString = prefs.getString(key, null) ?: return null

        try {
            val decoded = Base64.decode(storedString, Base64.DEFAULT)
            val iStream = ObjectInputStream(ByteArrayInputStream(decoded))
            @Suppress("UNCHECKED_CAST")
            return iStream.readObject() as? T
        } catch (error: Exception) {
            //Log.d(TAG, "Failed to de-serialise object for key: $key", error)
        }

        return null
    }

    /**
     * Used to store serialized object in preferences. First serializable object is converted to
     * Base64 byte array and passed to transformer function. The result of the function is stored
     * in preferences. Used for encrypting data.
     */
    fun setSerializable(key: String, value: Serializable, transformer: (ByteArray) -> ByteArray)
            : ApplicationPreferences {

        try {
            val byteStream = ByteArrayOutputStream(1024)
            val oStream = ObjectOutputStream(byteStream)
            oStream.writeObject(value)

            //Now encode the data
            editor.putString(key, Base64.encodeToString(transformer(byteStream.toByteArray()),
                    Base64.DEFAULT))
        } catch (error: IOException) {
            //Log.d(TAG, "Failed to serialise object for key: $key", error)
        }

        return this
    }

    /**
     * Used to retrieve store serialized object. transformer function is called before the object is
     * de-serialized. Used for decrypting previously encrypted object.
     */
    fun <T : Serializable> getSerializable(key: String, transformer: (ByteArray) -> ByteArray): T? {
        val storedString = prefs.getString(key, null) ?: return null

        try {
            val decoded = Base64.decode(storedString, Base64.DEFAULT)
            val iStream = ObjectInputStream(ByteArrayInputStream(transformer(decoded)))
            @Suppress("UNCHECKED_CAST")
            return iStream.readObject() as? T
        } catch (error: Exception) {
            //Log.d(TAG, "Failed to de-serialise object for key: $key", error)
        }

        return null
    }
}
