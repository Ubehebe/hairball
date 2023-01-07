package jvmutil.deps

import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType

fun Pair<String, String>.asDotAttr(): Pair<String, Attribute> =
    first to
        object : Attribute {
          override fun getType(): AttributeType = AttributeType.STRING
          override fun getValue(): String = second
        }
