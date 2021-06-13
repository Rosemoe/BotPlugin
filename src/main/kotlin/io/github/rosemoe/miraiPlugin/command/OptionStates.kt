package io.github.rosemoe.miraiPlugin.command

class OptionStates {

    val optionStates: MutableMap<String, String> = mutableMapOf()

    fun isOptionSet(name: String) : Boolean {
        return optionStates[name] != null
    }

    fun getOption(name: String) : String? {
        return optionStates[name]
    }

}