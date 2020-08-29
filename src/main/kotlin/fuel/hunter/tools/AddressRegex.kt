package fuel.hunter.tools

private fun String.clearBeforeDot() =
    indexOf(".")
        .takeIf { it > -1 }
        ?.let { substring(it + 1) }
        ?: this

fun String.toAddressRegex() =
    replace("iela", "")
        .clearBeforeDot()
        .split(" ")
        .joinToString(separator = ").+(", prefix = ".*(", postfix = ").*")
        .toRegex(RegexOption.IGNORE_CASE)

