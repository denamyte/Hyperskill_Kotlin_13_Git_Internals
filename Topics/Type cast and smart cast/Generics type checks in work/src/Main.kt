fun <T> countElementsOfType(list: List<Any>, clazz: Class<T>): Int {
    return list.count { clazz.isInstance(it) }
}