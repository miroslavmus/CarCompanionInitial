package uni.fmi.miroslav.carcompanion.models

import java.io.Serializable

data class ModelItem (
    val picturePath: String,
    val name: String,
    val changeField1Text: String,
    val changeField2Text: String,
    val valueField1Text: String,
    val valueField2Text: String,
    var message: String,
    val id: Int
) : Serializable {



}