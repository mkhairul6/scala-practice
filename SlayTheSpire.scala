import scala.collection.mutable._
import scala.util._

object Utils {
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
}

object SlayTheSpire extends App {
	val num = Utils.readNumber("Hello: ")
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

	var lastError : Option[String] = None

	def receiveDamage(monster : Monster) = {
	}

	def drawCards(size : Int) = {
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

	def play(cardNum : Int) : Unit = {
		if(cardNum > hand.size) {
			lastError = Some("Invalid card number")
			return
		}

		val card = hand(cardNum - 1)
		if(card.cost > energyLeft) {
			lastError = Some("Not enough energy")
			return
		}

		if(card.targetable) {
			val targetMonster = if(monsters.size > 1) monsters(Utils.readNumber("Select target monster: ")) else monsters(0)
			card.execute(this, monsters)
		}
		else if(card.cardType == CardType.Attack) {
			card.execute(this, monsters)
		}
		else if(card.cardType == CardType.Skill) {
			card.execute(this)
		}
		else if(card.cardType == CardType.Power) {
			card.execute(this)
		}

		energyLeft -= card.cost
		hand -= card
	}
}

class Monster(val name : String) {
	var hp = 0
	var maxhp = 0
	var armor = 0
	var barricade = false
	var thorns = 0
	var weakened = 0
	var vulnerabled = 0
	var strength = 0
	var abilities = new ArrayBuffer[Ability]
	var nextAbility : Option[Ability] = None

	override def toString() : String = {
		val options = new ArrayBuffer[String]
		if(vulnerabled > 0) {
			options.append(s"V${vulnerabled}")
		}
		if(weakened > 0) {
			options.append(s"W${weakened}")
		}
		if(strength > 0) {
			options.append(s"S${strength}")
		}
		val extra = options.mkString(" ")
		val ability = if(nextAbility.isEmpty) "" else nextAbility.get.getName(this)
		return s"$name (${hp}/${maxhp}) [${armor}] ${extra} ${ability}"
	}
}

class DarkCultist extends Monster("Dark Cultist") {
}

class Ability {
	def getName(monster : Monster) : String = ""
}

object CardType extends Enumeration {
	type CardType = Value
	val Attack, Skill, Power = Value
}

class Card(val cardType : CardType.Value, val cost : Int, val targetable : Boolean, val name : String) {
	var upgraded = false

	def execute(character : Character, monster : Monster) = ()
	def execute(character : Character, monsters : ArrayBuffer[Monster]) = ()
	def execute(character : Character) = ()

	def label : String = { "" }
	def fullName : String = { if(upgraded) s"$name+" else name }
}

class Strike extends Card(CardType.Attack, 1, true, "Strike") {
	override def label : String = { if(upgraded) "Deal 9 Damage" else "Deal 6 Damage" }
}
