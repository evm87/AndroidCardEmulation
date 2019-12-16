package com.evanmeyermann.cardemulationexample.nfc

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.util.Arrays
import android.util.Log
import com.evanmeyermann.cardemulationexample.home.ACTION_NDEF
import com.evanmeyermann.cardemulationexample.home.ACTION_UPDATE_CAN_BROADCAST
import com.evanmeyermann.cardemulationexample.home.BROADCAST_STATUS_HEADER
import com.evanmeyermann.cardemulationexample.home.NDEF_STATUS_MESSAGE
import java.lang.IllegalStateException
import kotlin.experimental.and

private const val TAG = "CardService"

/**
 * CardService handles communication with external card readers and internal card emulation via HostApduService
 */
class CardService : HostApduService() {

    // Static flag for switching on/off broadcasting of data
    private var mCanBroadcast: Boolean = false

    // State information
    private var mInitialized = false
    private var mState = State.NothingSelected

    // ByteArray for wrapping NdefMessage with identifier data to be sent
    private var ndefFile = ByteArray(0)

    internal enum class State {
        NothingSelected,
        AppSelected,
        ContainerCapabilitySelected,
        NdefSelected
    }

    companion object {
        /**
         * Utility method to convert a hexadecimal string to a byte string.
         *
         * Behavior with input strings containing non-hexadecimal characters is undefined.
         *
         * @param s String containing hexadecimal characters to convert
         * @return Byte array generated from input
         * @throws java.lang.IllegalArgumentException if input length is incorrect
         */
        @Throws(IllegalArgumentException::class)
        fun HexStringToByteArray(s: String): ByteArray {
            val len = s.length
            if (len % 2 == 1) {
                throw IllegalArgumentException("Hex string must have even number of characters")
            }
            val data = ByteArray(len / 2) // Allocate 1 byte per 2 hex characters
            var i = 0
            while (i < len) {
                // Convert each character into a integer (base-16), then bit-shift into place
                data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        /**
         * Build APDU for SELECT AID command. This command indicates which service a reader is
         * interested in communicating with. See ISO 7816-4.
         *
         * @param aid Application ID (AID) to select
         * @return APDU for SELECT AID command
         */
        fun BuildSelectApdu(aid: String): ByteArray {
            // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
            return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                    aid.length / 2) + aid)
        }

        // Byte codes for data exchange between receiver and CardService
        private val TAG = "CardService"
        // AID for our loyalty card service.
        private val SAMPLE_AID = "F222222222"
        // ISO-DEP command HEADER for selecting an AID.
        // Format: [Class | Instruction | Parameter 1 | Parameter 2]
        private val SELECT_APDU_HEADER = "00A40400"
        // "OK" status word sent in response to SELECT AID command (0x9000)
        private val SELECT_OK_SW = HexStringToByteArray("9000")
        // "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
        private val UNKNOWN_CMD_SW = HexStringToByteArray("0000")
        private val SELECT_APDU = BuildSelectApdu(SAMPLE_AID)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_NDEF -> {
                ndefFile = intent.getByteArrayExtra(NDEF_STATUS_MESSAGE)
            }
            ACTION_UPDATE_CAN_BROADCAST -> {
                mCanBroadcast = intent.getBooleanExtra(BROADCAST_STATUS_HEADER, true)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    /**
     * Called if the connection to the NFC card is lost, in order to let the application know the
     * cause for the disconnection (either a lost link, or another AID being selected by the
     * reader).
     *
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "DEACTIVATED:" + if (reason == DEACTIVATION_DESELECTED) "DESELECTED" else "LINK LOST")
        this.mState = State.NothingSelected
        this.mInitialized = false
    }

    /**
     * This method will be called when a command APDU has been received from a remote device. A
     * response APDU can be provided directly by returning a byte-array in this method. In general
     * response APDUs must be sent as quickly as possible, given the fact that the user is likely
     * holding his device over an NFC reader when this method is called.
     *
     * @param commandApdu The APDU that received from the remote device
     * @param extras A bundle containing extra data. May be null.
     * @return a byte-array containing the response APDU, or null if no response APDU can be sent
     * at this point.
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        if (!mCanBroadcast) {
            return null

        } else {
            Log.i(TAG, "Received APDU: " + commandApdu)
            // If the APDU matches the SELECT AID command for this service,
            // send the data, followed by a SELECT_OK status trailer (0x9000).
            return if (Arrays.equals(SELECT_APDU, commandApdu)) {
                Log.i(TAG, "Sending NDEF data")

                concatArrays(ndefFile, SELECT_OK_SW)
            } else {
                UNKNOWN_CMD_SW
            }
        }
    }

    /**
     * Utility method to concatenate two byte arrays.
     * @param first First array
     * @param rest Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    fun concatArrays(first: ByteArray, vararg rest: ByteArray): ByteArray {
        var totalLength = first.size
        for (array in rest) {
            totalLength += array.size
        }
        val result = Arrays.copyOf(first, totalLength)
        var offset = first.size
        for (array in rest) {
            System.arraycopy(array, 0, result, offset, array.size)
            offset += array.size
        }
        return result
    }

}