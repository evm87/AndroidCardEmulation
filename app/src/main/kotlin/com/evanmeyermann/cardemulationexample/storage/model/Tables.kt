package com.evanmeyermann.cardemulationexample.storage.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class DBCardConfig(@PrimaryKey
                         open var id: Int = 0,

                        open var timezone: String = "",

                        open var name: String = "",

                        open var cardNumber: String = "",

                        open var hasSecurityCode: Boolean = false,

                        open var securityCode: String = "") : RealmObject()


