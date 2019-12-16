package com.evanmeyermann.cardemulationexample.storage.model

class Model {

    /**
     * Card details domain model
     */
    data class CardDetails(
            val id : Int = 0,

            val timezone: String = "",

            val name: String = "",

            val cardNumber: String = "",

            val hasSecurityCode: Boolean = false,

            val securityCode: String = ""
    )
}