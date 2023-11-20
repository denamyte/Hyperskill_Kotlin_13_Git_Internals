fun convertToStringList(list: List<Any>) =
    list.map { it as? String ?: "N/A" }