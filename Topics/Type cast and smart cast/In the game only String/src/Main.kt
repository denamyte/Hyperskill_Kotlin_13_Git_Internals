fun <T> getStringsOnly(list: List<T>): List<String> =
    list.filterIsInstance<String>()