package net.tjalp.peach.peel.util

/**
 * Generate a random string [A-Za-z0-9]
 *
 * @param length The length of the [String]
 * @return The randomly generated [String]
 */
fun generateRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}