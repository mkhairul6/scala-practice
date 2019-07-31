class Run {

	static int readNumber(String msg) {
		while(true) {
			print "\n$msg";
			def line = System.console().readLine();
			if(line && line.number) {
				return Integer.parseInt(line);
			}
		}
	}

	static void main(String... args) {
		def character = new Character();
		character.init();
		while(true) {
			character.show();

			def card = readNumber("Select card (0 to end turn): ");
			if(card == 0) {
				character.endTurn();
			}
			else {
				character.play(card);
			}

		}
	}
}

class Character {
	int energy;
	int energyLeft;
	int hp;
	int maxHp;
	int handSize;
	int armor;
	boolean barricade;
	int thorns;
	int weakened;
	int frailed;
	int vulnerabled;
	List<Card> cards = [];
	List<Card> hand = [];
	List<Card> draw = [];
	List<Card> discard = [];
	List<Card> exhausted = [];
	List<Card> nextTurnEffects = [];
	List<Card> endTurnEffects = [];
	int turn;

	String lastError;

	List<Monster> monsters = [];

	void receiveDamage(Monster m) { }

	void drawCards(int size) {
		def toAdd2Hand = [];
		if(draw.size() >= size) {
			toAdd2Hand.addAll(draw.subList(0, size));
			size.times { draw.remove(0); }
		}
		else {
			toAdd2Hand.addAll(draw);
			def left = size - draw.size();
			draw.clear();
			draw.addAll(discard);
			discard.clear();
			Collections.shuffle(draw);
			toAdd2Hand.addAll(draw.subList(0, left));
			left.times { draw.remove(0); }
		}
		hand.addAll(toAdd2Hand);
	}

	void play(int cardNum) {
		if(cardNum > hand.size()) { lastError = "********* Invalid card number **********"; return }

		def card = hand[cardNum - 1];
		if(card.cost > energyLeft) { lastError = "********** Not enough energy **********"; return }

		if(card.targetable) {
			def targetMonster;
			if(monsters.size() > 1) {
				targetMonster = monsters[Run.readNumber("Select target monster: ")];
			}
			else {
				targetMonster = monsters[0];
			}
			card.execute(this, targetMonster);
		}
		else if(card.cardType == CardType.ATTACK) {
			card.execute(this, monsters);
		}
		else if(card.cardType == CardType.SKILL) {
			card.execute(this);
		}
		else if(card.cardType == CardType.POWER) {
			card.execute(this);
		}

		energyLeft -= card.cost;
		hand.remove(card);
		discard.add(card);
	}

	void init() {
		monsters = [ new DarkCultist() ];

		cards = [ new Defend(), new Defend(), new Defend(), new Defend(), new Defend(),
		    new Strike(), new Strike(), new Strike(), new Strike(), new Strike(), new Bash()
		];
		draw.addAll(cards);

		Collections.shuffle(draw);
		energy = 3;
		energyLeft = 3;
		hp = 70;
		maxHp = 70;
		handSize = 5;

		turn = 0;
		endTurn();
	}

	void startTurn() {
	}

	void endTurn() {
		monsters.each { it.endTurn(this); }

		discard.addAll(hand);
		hand.clear();
		drawCards(handSize);
		turn++;
		energyLeft = energy;

		armor = 0;
		if(weakened > 0) { weakened--; }
		if(frailed > 0) { frailed--; }
		if(vulnerabled > 0) { vulnerabled--; }
	}

	void show() {
		print("\033[H\033[2J");
		System.out.flush();
		println "Turn: ${turn}\n";
		println "Monsters:";
		monsters.eachWithIndex { it, ix ->
			println "${ix + 1}: $it";
		}
		println "\nHP: ${hp}/${maxHp} Block: ${armor} Energy: ${energyLeft}/${energy}";
		println "Draw: ${draw.size()} Hand: ${hand.size()}  Discard: ${discard.size()}";
		hand.eachWithIndex { it, ix ->
			println "${ix + 1}: [${it.cost}] ${it.name} <${it.label}>";
		}
		if(lastError) {
			println "\n$lastError";
			lastError = null;
		}
	}
}

class DarkCultist extends Monster {
	DarkCultist() {
		name = "Dark Cultist";
		maxhp = 48;
		hp = maxhp;
	}
}

abstract class Ability {
	float probabilityFrom;
	float probabilityTo;

	abstract String getName(Monster m);
	abstract void apply(Monster m, Character c);
}

class Attack extends Ability {
	int damage;

	Attack(float probabilityFrom, float probabilityTo, int damage) {
		this.probabilityFrom = probabilityFrom;
		this.probabilityTo = probabilityTo;
		this.damage = damage;
	}

	String getName(Monster m) {
		"Attack ${damage + m.strength}";
	}

	void apply(Monster m, Character c) {
		def finalDamage = damage + m.strength;
		if(c.armor) {
			if(c.armor < finalDamage) {
				def diff = finalDamage - c.armor;
				c.armor = 0;
				c.hp -= diff;
			}
			else {
				c.armor -= finalDamage;
			}
		}
		else {
			c.hp -= finalDamage;
		}
	}
}

class Ritual extends Ability {
	int x;

	Ritual(float probabilityFrom, float probabilityTo, int x) { 
		this.probabilityFrom = probabilityFrom;
		this.probabilityTo = probabilityTo;
	       	this.x = x;
	}

	String getName(Monster m) {
		"Ritual ${x}";
	}

	void apply(Monster m, Character c) {
		m.strength += x;
	}
}

class Monster {
	int hp;
	int maxhp;
	int armor;
	String name;
	boolean barricade;
	int thorns;
	int weakened;
	int vulnerabled;
	int strength;
	List<Ability> abilities = [ new Attack(0.0f, 0.49f, 6), new Ritual(0.5f, 1.0f, 5) ];
	Ability nextAbility;

	void endTurn(Character character) {
		if(nextAbility) {
			nextAbility.apply(this, character);
		}

		def random = Math.random();
		nextAbility = abilities.find { random >= it.probabilityFrom && random <= it.probabilityTo };

		armor = 0;
		if(weakened > 0) { weakened--; }
		if(vulnerabled > 0) { vulnerabled--; }
	}

	boolean receiveDamage(int damage) {
		if(vulnerabled) {
			damage += damage / 2;
		}
		def leftOver = damage - armor;
		hp -= leftOver;
		return hp > 0;
	}

	String toString() {
		List extra = [];
		if(vulnerabled) {
			extra << "V${vulnerabled}";
		}
		if(weakened) {
			extra << "W${weakened}";
		}
		if(strength) {
			extra << "S${strength}";
		}
		return "$name (${hp}/${maxhp}) [${armor}] ${extra.join(' ')} ${nextAbility?.getName(this)}";
	}
}

class Floor {
}

class Defect extends Character {
}

class Card {
	int getCost() { 0 }
	String getName() { }
	String getLabel() { }

	boolean upgraded;
	boolean upgradePermanent;
	CardType getCardType() { }
	boolean getTargetable() { false }

	String getUg() { upgraded ? "+" : "" }

	void execute(Character c) { }
	void execute(Character c, Monster m) { }
	void execute(Character c, List<Monster> m) { }
	void nextTurnEffect(Character c) { }
	void endTurnEffect(Character c) { }
}

class Bash extends Card {
	CardType getCardType() { CardType.ATTACK }
	boolean getTargetable() { true }
	int getCost() { 2 }
	String getName() { "Bash${ug}" }
	String getLabel() { upgraded ? "Deal 10 damage. Apply 3 Vulnerable." : "Deal 8 damage. Apply 2 Vulnerable." }
	int getVulnerable() { upgraded ? 3 : 2 }
	int getDamage() { upgraded ? 10 : 8 }


	void execute(Character c, Monster m) {
		m.receiveDamage(upgraded ? 9 : 6);
		m.vulnerabled += vulnerable;
	}
}

class Defend extends Card {
	CardType getCardType() { CardType.SKILL }
	int getCost() { 1 }
	String getName() { "Defend${ug}" }
	String getLabel() { "Gain ${upgraded ? 8 : 5} Block" }

	void execute(Character c) {
		c.armor += upgraded ? 8 : 5
	}
}

class Strike extends Card {
	CardType getCardType() { CardType.ATTACK }
	boolean getTargetable() { true }
	int getCost() { 1 }
	String getName() { "Strike${ug}" }
	String getLabel() { "Deal ${upgraded ? 9 : 6} Damage" }

	void execute(Character c, Monster m) {
		m.receiveDamage(upgraded ? 9 : 6);
	}
}

enum CardType {
	SKILL, ATTACK, POWER
}
