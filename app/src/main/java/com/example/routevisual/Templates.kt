package com.example.routevisual

class Templates {
    companion object {
        val straightPairs: ArrayList<Pair<String, String>> = arrayListOf(
            Pair("пройти", "прямо"),
            Pair("пойти", "прямо"),
            Pair("пройти", "вперёд"),
            Pair("пойти", "вперёд"),
            Pair("пройти", "по улице"),
            Pair("пойти", "по улице"),
            Pair("пройти", "вдоль улицы"),
            Pair("пойти", "вдоль улицы"),
            Pair("иду", "прямо"),
            Pair("идти", "прямо"),
            Pair("движение", "прямо"),
            Pair("двигаться", "прямо"),
            Pair("двигаюсь", "прямо"),
        )

        val straightSingles: ArrayList<String> = arrayListOf(
            "вперёд",
            "прямо",
            "пройти",
            "прохожу",
            "идти",
            "иду",
        )

        val sideTurns: ArrayList<String> = arrayListOf(
            "повернуть",
            "поворот",
            "поворачиваю",
            "повернуться",
            "пойти",
            "иду",
            "идти",
        )

        val backTurns: ArrayList<String> = arrayListOf(
            "развернуться",
            "разворот",
        )

        val left: ArrayList<String> = arrayListOf(
            "налево",
            "влево",
        )
        val right: ArrayList<String> = arrayListOf(
            "направо",
            "вправо",
        )
        val back: ArrayList<String> = arrayListOf(
            "назад",
            "обратно",
        )
    }
}