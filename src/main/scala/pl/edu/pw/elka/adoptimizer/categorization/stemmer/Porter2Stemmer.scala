package classifier.stemmer

/**
 *
 * Porter stemmer in Scala. The original paper is in
 *
 * Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14,
 * no. 3, pp 130-137,
 *
 * See also http://www.tartarus.org/~martin/PorterStemmer
 *
 * A few methods were borrowed from the existing Java port from the above page.
 *
 */

import scala.io.Source

class Porter2Stemmer {
  private var b = ""

  private def cons(i: Int): Boolean = {
    var ch = b(i)

    var vowels = "aeiou"

    if (vowels.contains(ch))
      return false

    if (ch == 'y') {
      if (i == 0) {
        return true
      } else {
        return !cons(i - 1)
      }
    }

    return true
  }

  private def calcM(s: String): Int = {
    var l = s.length
    var count = 0
    var currentConst = false

    for (c <- 0 to l - 1) {
      if (cons(c)) {
        if (!currentConst && c != 0) {
          count += 1
        }
        currentConst = true
      } else {
        currentConst = false
      }
    }

    return count
  }

  private def vowelInStem(s: String): Boolean = {
    for (i <- 0 to b.length - 1 - s.length) {
      if (!cons(i)) {
        return true
      }
    }

    return false
  }

  private def doublec(): Boolean = {
    var l = b.length - 1

    if (l < 1)
      return false

    if (b(l) != b(l - 1))
      return false

    return cons(l)
  }

  private def cvc(s: String): Boolean = {
    var i = b.length - 1 - s.length
    if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2))
      return false;

    var ch = b(i)

    var vals = "wxy"

    if (vals.contains(ch))
      return false

    return true
  }

  private def replacer(orig: String, replace: String, checker: Int => Boolean): Boolean = {
    var l = b.length
    var origLength = orig.length
    var res = false

    if (b.endsWith(orig)) {
      var n = b.substring(0, l - origLength)

      var m = calcM(n)
      if (checker(m)) {
        b = n + replace
      }

      res = true
    }

    return res
  }

  private def processSubList(l: List[(String, String)], checker: Int => Boolean): Boolean = {
    var iter = l.iterator
    var done = false

    while (!done && iter.hasNext) {
      var v = iter.next
      done = replacer(v._1, v._2, checker)
    }

    return done
  }

  private def step1() {
    var l = b.length

    var m = calcM(b)

    var vals = List(("sses", "ss"), ("ies", "i"), ("ss", "ss"), ("s", ""))
    processSubList(vals, _ >= 0)

    if (!(replacer("eed", "ee", _ > 0))) {
      if ((vowelInStem("ed") && replacer("ed", "", _ >= 0)) || (vowelInStem("ing") && replacer("ing", "", _ >= 0))) {
        vals = List(("at", "ate"), ("bl", "ble"), ("iz", "ize"))

        if (!processSubList(vals, _ >= 0)) {
          m = calcM(b)
          var last = b(b.length - 1)
          if (doublec() && !"lsz".contains(last)) {
            b = b.substring(0, b.length - 1)
          } else if (m == 1 && cvc("")) {
            b = b + "e"
          }
        }
      }
    }

    (vowelInStem("y") && replacer("y", "i", _ >= 0))
  }

  private def step2() = {
    var vals = List(("ational", "ate"), ("tional", "tion"), ("enci", "ence"), ("anci", "ance"), ("izer", "ize"), ("bli", "ble"), ("alli", "al"),
      ("entli", "ent"), ("eli", "e"), ("ousli", "ous"), ("ization", "ize"), ("ation", "ate"), ("ator", "ate"), ("alism", "al"),
      ("iveness", "ive"), ("fulness", "ful"), ("ousness", "ous"), ("aliti", "al"), ("iviti", "ive"), ("biliti", "ble"), ("logi", "log"))

    processSubList(vals, _ > 0)
  }

  private def step3() = {
    var vals = List(("icate", "ic"), ("ative", ""), ("alize", "al"), ("iciti", "ic"), ("ical", "ic"), ("ful", ""), ("ness", ""))

    processSubList(vals, _ > 0)
  }

  private def step4() = {
    var vals = List(("al", ""), ("ance", ""), ("ence", ""), ("er", ""), ("ic", ""), ("able", ""), ("ible", ""), ("ant", ""), ("ement", ""),
      ("ment", ""), ("ent", ""))

    var res = processSubList(vals, _ > 1)

    if (!res) {
      if (b.length > 4) {
        if (b(b.length - 4) == 's' || b(b.length - 4) == 't') {
          res = replacer("ion", "", _ > 1)
        }
      }
    }

    if (!res) {
      var vals = List(("ou", ""), ("ism", ""), ("ate", ""), ("iti", ""), ("ous", ""), ("ive", ""), ("ize", ""))
      res = processSubList(vals, _ > 1)
    }
  }

  private def step5a() = {
    var res = false
    res = replacer("e", "", _ > 1)

    if (!cvc("e")) {
      res = replacer("e", "", _ == 1)
    }
  }

  private def step5b() = {
    var res = false
    var m = calcM(b)
    if (m > 1 && doublec() && b.endsWith("l")) {
      b = b.substring(0, b.length - 1)
    }
  }

  def stem(word: String): String = {
    b = word

    if (b.length > 2) {
      step1()
      step2()
      step3()
      step4()
      step5a()
      step5b()
    }

    return b
  }
}

