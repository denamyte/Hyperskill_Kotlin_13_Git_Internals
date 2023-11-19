fun digitSum(n: Int): Int =
    if (n < 10) n
    else n % 10 + digitSum(n / 10)
