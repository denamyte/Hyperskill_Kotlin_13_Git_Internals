// You can experiment here, it won’t be checked

fun main(args: Array<String>) {
  f(2)
  println(sum)
}

var sum = 0
fun f(n : Int)
{
  sum += n
  println(n)
  if(n < 5)
  {
    f(n+1)
    f(n+3)
  }
}


