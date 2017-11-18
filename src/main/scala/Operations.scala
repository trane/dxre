package com.errstr

sealed trait Regex {
  def nullable: Boolean
  def derive(c: Char): Regex
  def matches(str: String): Boolean = str.length match {
    case 0 => nullable
    case n => derive(str.head).matches(str.tail)
  }
}

object Regex {

  case object Empty extends Regex {
    // δ(∅) = false
    val nullable = false
    // Dc(∅) = ∅
    def derive(c: Char): Regex = Empty
  }

  case object Blank extends Regex {
    val nullable = true
    def derive(c: Char): Regex = Empty
  }

  case class Single(c: Char) extends Regex {
    val nullable = false
    def derive(c: Char): Regex = if (c == this.c) Blank else Empty
  }

  case class Choice(left: Regex, right: Regex) extends Regex {
    lazy val nullable = left.nullable || right.nullable
    def derive(c: Char): Regex = Choice(left.derive(c), right.derive(c))
  }

  case class Sequence(left: Regex, right: Regex) extends Regex {
    lazy val nullable = left.nullable && right.nullable
    /**
     *  Dc(A ○ B) = Dc(A) ○ B if A does not contain the empty string
     *  Dc(A ○ B) = Dc(A) ○ B ∪ Dc(B) if A contains the empty string 
     */
    def derive(c: Char): Regex =
      if (left.nullable) Choice(Sequence(left.derive(c), right), right.derive(c))
      else Sequence(left.derive(c), right)
  }

  case class Repetition(base: Regex) extends Regex {
    val nullable = true
    def derive(c: Char): Regex = Sequence(base.derive(c), base)
  }

  case class Intersection(l1: Regex, l2: Regex) extends Regex {
    lazy val nullable = l1.nullable && l2.nullable
    def derive(c: Char): Regex = Intersection(l1.derive(c), l2.derive(c))
  }

  case class Difference(x: Regex, y: Regex) extends Regex {
    lazy val nullable = x.nullable && !y.nullable
    def derive(c: Char): Regex = Difference(x.derive(c), y.derive(c))
  }

  case class Complement(base: Regex) extends Regex {
    lazy val nullable = !base.nullable
    def derive(c: Char): Regex = Complement(base.derive(c))
  }
}
