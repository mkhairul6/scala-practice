import java.util._

object HelloWorld extends App {
	def readNumber(msg: String) : Int = {
		val console = System.console()
		val line = console.readLine()
	}

	val list = new ArrayList[String]
	list.add("Hello")
	list.add("World")
	println("Hello World")
	println(list)

	println(readNumber())

	println(list.getClass())
}


