import scala.collection.mutable._
import scala.util._

object SlayTheSpire extends App {

	def readNumber(msg: String) : Int = {
		while(true) {
			print(s"$msg")
			val line = System.console().readLine()
			if(line != null) {
				try {
					return Integer.parseInt(line)
				}
				catch {
					case e : NumberFormatException => ()
				}
			}
		}
		return -1
	}

	val num = readNumber("Hello: ")
	println(num)
}

class Character {
	var energy = 0
	var energyLeft = 0
	var hp = 0
	var maxHp = 0
	var handSize = 5
	var armor = 5
	var barricade = false
	var thorns = 0
	var weakened = 0
	var frailed = 0
	var vulnerabled = 0
	var turn = 0

	var cards = new ArrayBuffer[Card]()
	var hand = new ArrayBuffer[Card]()
	var draw = new ArrayBuffer[Card]()
	var discard = new ArrayBuffer[Card]()
	var exhausted = new ArrayBuffer[Card]()

	var monsters = new ArrayBuffer[Monster]()

	var lastError : String = null

	def receiveDamage(monster : Monster) : Unit = {
	}

	def drawCards(size : Int) : Unit = {
		var toAdd2Hand = new ArrayBuffer[Card]
		if(draw.size >= size) {
			toAdd2Hand.appendAll(draw.slice(0, size))
			draw.remove(0, size)
		}
		else {
			toAdd2Hand.appendAll(draw)
			val left = size - draw.size
			draw.clear()
			draw.appendAll(discard)
			discard.clear()
			draw = new Random().shuffle(draw)
			toAdd2Hand.appendAll(draw.slice(0, left))
			draw.remove(0, left)
		}
		hand.appendAll(toAdd2Hand)
	}
}

class Monster {
}

class Card {
}
