package technology.iatlas.spaceup.dto

/**
 * Represent any kind of feedback, e.g. successfully reloaded a services or a domain
 */
data class Feedback(var info: String, var error: String)