package io.github.rosemoe.botPlugin

class Disabled : RuntimeException() {

    companion object {
        @JvmStatic
        val INSTANCE: Disabled = Disabled()
    }
}