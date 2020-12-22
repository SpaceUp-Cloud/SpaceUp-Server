package technology.iatlas.spaceup.dto

data class Domain(val url: String) {
    /*
        A method for html friendly value.
        It repalces every dot with an underscore.
     */
    fun htmlFriendly(): String {
        return url.replace(".", "_")
    }
}
