fun multiplyInts(list: List<Any>) =
    list.map { (it as? Int)?.times(2) ?: it }