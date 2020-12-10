package com.example.routevisual

class TextToManeuversConverter {
    companion object {
        private lateinit var slicedText: MutableList<String>
        private var maneuvers: MutableList<Maneuver> = arrayListOf()
        private var maneuversString: MutableList<String> = arrayListOf("")

        fun convertText(text: String): Pair<MutableList<Maneuver>, MutableList<String>> {
            slicedText = text.split(' ').toMutableList()
            if (!MapsActivity.stagedModeEnabled) {
                maneuvers.clear()
                maneuversString.clear()
            }
            while (slicedText.size > 0) {
                if (parsedFirstAmountPosition()) continue
                if (parsedDirection()) continue
                if (parsedMiddleAmountPosition()) continue
                if (parsedEndAmountPosition()) continue
                if (parsedTurn()) continue
                if (slicedText.isNotEmpty())
                    slicedText.removeFirst()
            }
            return Pair(maneuvers, maneuversString)
        }

        private fun getAmountMeters(index: Int = 0): Int {
            val twoWords =
                if (slicedText.size > index + 1) Pair(slicedText[index], slicedText[index + 1])
                else Pair(slicedText[index], "")
            var result: Int
            return try {
                result = twoWords.first.toInt()
                if (twoWords.second.equals("км", true)
                    || twoWords.second.equals("km", true)
                    || twoWords.second.contains("километр", true)
                    || twoWords.second.contains("kilometer", true)
                ) {
                    result *= 1000
                }
                putAndCut(2)
                result
            } catch (ex: Exception) {
                if (twoWords.first[twoWords.first.lastIndex - 1].isDigit()) {
                    result = twoWords.first.substring(0, twoWords.first.lastIndex).toInt()
                } else {
                    result =
                        twoWords.first.substring(0, twoWords.first.lastIndex - 1).toInt()
                    result *= 1000
                }
                putAndCut(1)
                result
            }
        }

        private fun parsedFirstAmountPosition(): Boolean {
            if (slicedText.isEmpty()) return false
            if (slicedText.first()[0].isDigit()) {
                maneuversString.add("")
                val amountMeters = getAmountMeters()
                maneuvers.add(Maneuver(ManeuverType.STRAIGHT, amountMeters))
                if (slicedText.size == 1) {
                    putAndCut(1)
                } else {
                    Templates.straightPairs.forEach {
                        if (slicedText.size > 1 && slicedText[1].equals(it.second, true)) {
                            putAndCut(2)
                            return true
                        }
                    }
                    Templates.straightSingles.forEach {
                        if (slicedText.size > 1 && slicedText[0].equals(it, true)) {
                            putAndCut(1)
                            return true
                        }
                    }
                }
            }
            return false
        }

        private fun parsedDirection(afterTurnTemplate: Boolean = false): Boolean {
            if (slicedText.isEmpty()) return false
            Templates.left.forEach {
                if (slicedText[0].equals(it, true)) {
                    if (!afterTurnTemplate) maneuversString.add("")
                    maneuvers.add(Maneuver(ManeuverType.LEFT_TURN))
                    putAndCut(1)
                    return true
                }
            }
            Templates.right.forEach {
                if (slicedText[0].equals(it, true)) {
                    if (!afterTurnTemplate) maneuversString.add("")
                    maneuvers.add(Maneuver(ManeuverType.RIGHT_TURN))
                    putAndCut(1)
                    return true
                }
            }
            Templates.back.forEach {
                if (slicedText[0].equals(it, true)) {
                    if (!afterTurnTemplate) maneuversString.add("")
                    maneuvers.add(Maneuver(ManeuverType.BACK_TURN))
                    putAndCut(1)
                    return true
                }
            }
            return false
        }

        private fun parsedMiddleAmountPosition(): Boolean {
            if (slicedText.isEmpty()) return false
            Templates.straightPairs.forEach {
                if (slicedText.size > 2
                    && slicedText[0].equals(it.first, true)
                    && slicedText[1][0].isDigit()
                    && slicedText[2].equals(it.second, true)
                ) {
                    maneuversString.add("")
                    putAndCut(1)
                    val amountMeters = getAmountMeters()
                    putAndCut(1)
                    maneuvers.add(Maneuver(ManeuverType.STRAIGHT, amountMeters))
                    return true
                }
            }
            return false
        }

        private fun parsedEndAmountPosition(): Boolean {
            if (slicedText.isEmpty()) return false
            Templates.straightPairs.forEach {
                if (slicedText.size > 2
                    && slicedText[0].equals(it.first, true)
                    && slicedText[1].equals(it.second, true)
                    && slicedText[2][0].isDigit()
                ) {
                    maneuversString.add("")
                    putAndCut(2)
                    val amountMeters = getAmountMeters()
                    maneuvers.add(Maneuver(ManeuverType.STRAIGHT, amountMeters))
                    return true
                }
            }

            Templates.straightSingles.forEach {
                if (slicedText.size > 1
                    && slicedText[0].equals(it, true)
                    && slicedText[1][0].isDigit()
                ) {
                    maneuversString.add("")
                    putAndCut(1)
                    val amountMeters = getAmountMeters()
                    maneuvers.add(Maneuver(ManeuverType.STRAIGHT, amountMeters))
                    return true
                }
            }

            return false
        }

        private fun parsedTurn(): Boolean {
            if (slicedText.isEmpty()) return false
            Templates.sideTurns.forEach {
                if (slicedText[0].equals(it, true)) {
                    maneuversString.add("")
                    putAndCut(1)
                    parsedDirection(true)
                    return true
                }
            }
            Templates.backTurns.forEach {
                if (slicedText[0].equals(it, true)) {
                    maneuversString.add("")
                    putAndCut(1)
                    if (!parsedDirection(true))
                        maneuvers.add(Maneuver(ManeuverType.BACK_TURN))
                    return true
                }
            }
            return false
        }

        private fun putAndCut(amount: Int) {
            repeat(amount) {
                maneuversString[maneuversString.lastIndex] =
                    maneuversString.last() + "${slicedText.first()} "
                slicedText.removeFirst()
            }
        }

        private fun getNumberFromWord(word: String): Int {
            return when (word) {
                "один" -> 1
                "два" -> 2
                "три" -> 3
                "четыре" -> 4
                "пять" -> 5
                "шесть" -> 6
                "семь" -> 7
                "восемь" -> 8
                "девять" -> 9
                "десять" -> 10
                "одиннадцать" -> 11
                "двенадцать" -> 12
                "тринадцать" -> 13
                "четырнадцать" -> 14
                "пятнадцать" -> 15
                "шестнадцать" -> 16
                "семнадцать" -> 17
                "восемнадцать" -> 18
                "девятнадцать" -> 19
                "двадцать" -> 20
                "тридцать" -> 30
                "сорок" -> 40
                "пятьдесят" -> 50
                "шестьдесят" -> 60
                "семьдесят" -> 70
                "восемьдесят" -> 80
                "девяносто" -> 90
                "сто" -> 100
                "двести" -> 200
                "триста" -> 300
                "четыреста" -> 400
                "пятьсот" -> 500
                "шестьсот" -> 600
                "семьсот" -> 700
                "восемьсот" -> 800
                "девятьсот" -> 900
                "тысяча" -> 1000
                else -> -1
            }
        }
    }
}
