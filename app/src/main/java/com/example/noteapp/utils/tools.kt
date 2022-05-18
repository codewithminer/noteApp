package com.example.noteapp.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primecalendar.common.operators.DayOfMonth
import com.aminography.primecalendar.common.operators.plusAssign
import com.aminography.primecalendar.persian.PersianCalendar
import com.example.noteapp.model.data.DateModel

enum class LockStates{
    CreateLock, ChangeLock,
    EnterNote, RemoveLock,
    RemoveLockedNote
}

fun setTime(hour: Int, minute: Int): String {
    var str: String = ""
    if (hour < 10 && minute < 10)
        str = "0${hour}:0${minute}"
    else if (hour < 10)
        str = "0${hour}:${minute}"
    else if (minute < 10)
        str = "${hour}:0${minute}"
    else
        str = "${hour}:${minute}"
    return str
}

fun setPersianNumber(num: String): String {
    return num
        .replace("0", "۰")
        .replace("1", "۱")
        .replace("2", "۲")
        .replace("3", "۳")
        .replace("4", "۴")
        .replace("5", "۵")
        .replace("6", "۶")
        .replace("7", "۷")
        .replace("8", "۸")
        .replace("9", "۹")
}

fun getBackgroundColor(index: Int): String{
    return when(index){
        1 -> "#F2F6F8"
        2 -> "#3c3c3c"
        3 -> "#FFBABA"
        4 -> "#A9C7B5"
        5 -> "#A5CFDA"
        6 -> "#F1EBA3"
        7 -> "#FEC2D6"
        else -> "#F2F6F8"
    }
}

fun getForegroundColor(index: Int): String{
    return when(index){
        1 -> "#333333"
        2 -> "#f4f4f4"
        3 -> "#FF5252"
        4 -> "#385546"
        5 -> "#4896B8"
        6 -> "#A98600"
        7 -> "#F5347F"
        else -> "#333333"
    }
}


fun markDown(content: String, color: Int): String {
    var background = "#FFFFFF"
    var foreground = "#000000"
    var linkColor = ""
    var highlightBGC = ""
    var highlightColor = ""
    when(color){
        1 ->{ background = "#F2F6F8"
            foreground = "#333333"
            linkColor = "#4896B8"
            highlightBGC = "#333333"
            highlightColor = "#f4f4f4"}

        2 ->{ background = "#3c3c3c"
            foreground = "#f4f4f4"
            linkColor = "#FF5252"
            highlightBGC = "#f4f4f4"
            highlightColor = "#333333"}

        3 ->{ background = "#FFBABA"
            foreground = "#FF5252"
            linkColor = "#7f0000"
            highlightBGC = "#FF5252"
            highlightColor = "#f4f4f4"}

        4 ->{ background = "#A9C7B5"
            foreground = "#385546"
            linkColor = "#003300"
            highlightBGC = "#385546"
            highlightColor = "#f4f4f4"}

        5 ->{ background = "#A5CFDA"
            foreground = "#4896B8"
            linkColor = "#002f6c"
            highlightBGC = "#4896B8"
            highlightColor = "#f4f4f4"}

        6 ->{ background = "#F1EBA3"
            foreground = "#A98600"
            linkColor = "#524c00"
            highlightBGC = "#A98600"
            highlightColor = "#f4f4f4"}

        7 ->{ background = "#FEC2D6"
            foreground = "#F5347F"
            linkColor = "#560027"
            highlightBGC = "#F5347F"
            highlightColor = "#f4f4f4"}

    }

    val tag = "mark"
    var result = ""
    var htmlText = ""
    var previousLine = ""
    var flagLI = false

    val line = splitTextByLine(content)
    for (i in line.indices) {
        if (!isLi(line[i]) && UL_FLAG) {    // if new line is not li and ul tag is open, close ul tag and...
            result = makeBulletList(previousLine)
            previousLine = ""
            htmlText += result
            UL_FLAG = false
        }
        result = makeBold(line[i])
        result = makeItalics(result)
        result = makeHighLight(result)
        result = makeLink(result)
        result = makeLiTag(result)
        result = makeHeading(result)
        if (!Heading && !UL_FLAG && isPersianWord(result))
            result = "<p dir=\"rtl\">$result</p>"
        else if (!Heading && !UL_FLAG && !isPersianWord(result))
            result = "<p>$result</p>"
        result = "$result\n"
        if (!UL_FLAG)
            htmlText += result
        else
            previousLine += result  // - banana\n - apple\n ...
    }
    if (UL_FLAG) {
        result = makeBulletList(previousLine)
        htmlText += result
        UL_FLAG = false
    }


    Log.i("tag", htmlText)
    return "<html lang=\"fa-IR\">\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\">"+
            "<style>\n" +
            "mark{\n" +
            "background-color: $highlightBGC;\n" +
            "padding: 2px 2px;"+
            "color: $highlightColor;}\n" +
            "p{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "h1{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "h2{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "h3{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "h4{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "h5{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "h6{\n" +
            "color: $foreground;\n"+
            "}\n"+
            "a:link {\n" +
            "  color: $linkColor;\n" +
            "  background-color: transparent;\n" +
            "}"+
            "li{\n" +
            "color:$foreground;\n"+
            "}\n"+
            "</style>\n" +
            "</head>\n" +
            "<body style = background-color:$background \">\n" +
            "$htmlText\n<br></br><br></br><br></br>" +
            "</body>" +
            "</html>"
}

fun isLi(content: String): Boolean {
    val words = splitWordByWhitespace(content)
    if (words.size <= 1)    // if text is one word probably its not a exception
        return false
    if ((words.first().length == 1 && words.first()[0] == '-') ||  // if word is (- text) or (## - text) its a li exception
        (words[1].length == 1 && words[1][0] == '-' && countHashtag(words) != -1)
    ) {
        return true
    }
    return false
}

fun makeBulletList(content: String): String {
    var dir = "dir=\"ltr\""
    if (isPersianWord(content)){
        dir = "dir=\"rtl\""
    }
    return "<ul $dir>\n $content </ul>\n"
}

fun makeLiTag(content: String): String {

    var words = splitWordByWhitespace(content)
    if (words.size <= 1)
        return content
    var line = ""
    if (words.first().length == 1 && words.first()[0] == '-') {
        line = words.subList(1, words.size).joinToString(" ")
        line = "<li>$line</li>\n"
        UL_FLAG = true
    } else if (words[1].length == 1 && words[1][0] == '-' && countHashtag(words) != -1) {
        val tag = words[0]
        line = words.subList(2, words.size).joinToString(" ")
//        line = "$tag <li>$line</li>\n"
        val num = countHashtag(listOf(tag,"dummy"))
        line = "<li><h$num>$line</h$num></li>\n"
        UL_FLAG = true
    } else {
        line = content
    }
    return line
}

fun splitTextByLine(content: String): List<String> {
    return content.split("\n")
}

fun splitWordByWhitespace(word: String): List<String> {
    return word.split("\\s+".toRegex()).map {
        it.replace("""^[\.]|[\.]$""".toRegex(), "")
    }
}

fun makeHeading(content: String): String {
    var words = splitWordByWhitespace(content)
    val counter = countHashtag(words)
    if (counter == -1) {
        Heading = false
        return content
    }
    Heading = true
    words = words.subList(1, words.size)
    val line = words.joinToString(" ")
    var dir = "dir=\"ltr\""
    if (isPersianWord(line)){
        dir = "dir=\"rtl\""
    }
    return when (counter) {
        1 -> "<h1 $dir>$line</h1>\n"
        2 -> "<h2 $dir>$line</h2>\n"
        3 -> "<h3 $dir>$line</h3>\n"
        4 -> "<h4 $dir>$line</h4>\n"
        5 -> "<h5 $dir>$line</h5>\n"
        6 -> "<h6 $dir>$line</h6>\n"
        else -> content
    }
}

fun makeHighLight(content: String): String {
    if (content == "")
        return content
    var text = content
    val positions = arrayListOf<Int>()
    for (i in content.indices){
        if (content[i] == '`'){
            positions.add(i)
            if (positions.size == 2){
                var world = content.substring(positions[0]+1, positions[1])
                world = "<mark>$world</mark>"
                text = content.replaceRange(positions[0],positions[1]+1,world)
                Log.i("markdown",text)
                return makeHighLight(text)
            }
        }
    }
    return text
}

fun makeLink(content: String): String {
    var text = content
    var squareF1 = false
    var squareF2 = false
    var bracketsF1 = false
    var bracketsF2 = false
    var square1 = -1
    var square2 = -1
    var brackets1 = -1
    var brackets2 = -1
    for (i in content.indices) {
        if (content[i] == '[' && !squareF1) {
            square1 = i
            squareF1 = true
        }
        if (content[i] == ']' && !squareF2) {
            square2 = i
            squareF2 = true
        }

        if (content[i] == '(' && !bracketsF1) {
            brackets1 = i
            bracketsF1 = true
        }

        if (content[i] == ')' && !bracketsF2) {
            brackets2 = i
            bracketsF2 = true
        }
        if ((square1 < square2) && (brackets1 < brackets2) && (square2 < brackets1) && (square1 != -1 && square2 != -1 && brackets1 != -1 && brackets2 != -1)) {
            val w = content.substring(square1 + 1, square2)
            val link = content.substring(brackets1 + 1, brackets2)
            text = content.replaceRange(square1, brackets2 + 1, "<a href=\"$link\">$w</a>")
            return makeLink(text)
        }
    }
    return text
}

fun makeItalics(content: String): String {
    if (content == "")
        return content
    var text = content
    val positions = arrayListOf<Int>()
    for (i in content.indices){
        if (content[i] == '*'){
            positions.add(i)
            if (positions.size == 2){
                var world = content.substring(positions[0]+1, positions[1])
                Log.i("markdown",text)
                world = "<em>$world</em>"
                text = content.replaceRange(positions[0],positions[1]+1,world)
                Log.i("markdown",text)
                return makeItalics(text)
            }
        }
    }
    return text
}

fun makeBold(content: String): String {
    if (content == "")
        return content
    var text = content
    var start = -1
    var end = -1
    var c = 0
    for (i in content.indices){
        if (content[i] == '*' && i < content.length-1){
            if (content[i+1] == '*'){
                if (c==0)
                    start = i
                if (c==1)
                    end = i
            }else if(start!=-1)
                c++
            if (start!=-1 && end!=-1){
                var world = content.substring(start+2, end)
                Log.i("markdown",text)
                world = "<strong>$world</strong>"
                text = content.replaceRange(start,end+2,world)
                Log.i("markdown",text)
                return makeBold(text)
            }
        }
    }


//    var counter = 0
//    val positions = arrayListOf<Int>()
//    for (i in content.indices){
//        if (content[i] == '*' && i < content.length-1){
//            if (content[i+1] == '*'){
//                if (positions.size > 0)
//                    if (i - positions[counter-1] < 2)
//                        continue
//                positions.add(i)
//                counter++
//                if (positions.size == 2){
//                    var world = content.substring(positions[0]+2, positions[1])
//                    Log.i("markdown",text)
//                    world = "<strong>$world</strong>"
//                    text = content.replaceRange(positions[0],positions[1]+2,world)
//                    Log.i("markdown",text)
//                    return makeBold(text)
//                }
//            }
//        }
//    }
    return text

//    val words = splitWordByWhitespace(content).toMutableList()
//    for (i in 0 until words.size) {
//        if (words[i].length >= 5) {
//            var word = words[i]
//            if (word[0] == '*' && word[1] == '*' && word[word.length - 1] == '*' && word[word.length - 2] == '*') {
//                val w = word.substring(2, word.length - 2) // delete *s
//                words[i] = "<strong>$w</strong>"
//            }
//        }
//    }
//    return words.joinToString(" ")
}

fun countHashtag(words: List<String>): Int {
    var counter = 0
    if (words.first().length > 6)
        return -1
    for (i in words.first().indices) {
        if (words.first()[i] == '#')
            counter++
        else if (words.first()[i] != '#')
            return -1
    }
    return counter
}


fun test() {

//    val a:Int? = null
//    var b: Int = 2
//    if (a != null)
//        b = a
//    Log.i("zz", b.toString())
    val date = createDate()
    for (i in 0 until date.size) {
        if (date[i].Year.toInt() == 1400 &&
            date[i].Month == 11 &&
            date[i].Day.toInt() == 8
        ){
            Log.i("zz", "i found it in index: $i")
        }
    }
}

private fun createDate(): MutableList<DateModel> {

    val list = mutableListOf<DateModel>()
    val persian = PersianCalendar()

    val y = persian.year + 1
    var counterMonth = persian.month
    for (i in persian.year..y) {
        for (j in counterMonth..11) {
            persian.month = j
            val e = persian.monthLength - persian.dayOfMonth
            for (k in persian.dayOfMonth..persian.monthLength) {
                persian.dayOfMonth = k
                Log.i(
                    "date",
                    setPersianNumber(k.toString()) + ", " + persian.weekDayNameShort + ", " + persian.monthName + ", " + i
                )
                list += DateModel(
                    setPersianNumber(i.toString()),
                    persian.monthName,
                    persian.month,
                    setPersianNumber(k.toString()),
                    persian.weekDayNameShort
                )
            }
            persian.dayOfMonth -= e
            persian += DayOfMonth(e + 1)
            counterMonth++

        }
        counterMonth = 0
    }

    return list
}


fun isPersianWord(s: String): Boolean {
    for (i in 0 until Character.codePointCount(s, 0, s.length)) {
        val c = s.codePointAt(i)
        if (c in 0x0600..0x06FF || c in 0xFB50..0xFDFF || c in 0xFE70..0xFEFF)
            return true
    }
    return false
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

private fun convertCivilCalendarToPersianCalendar(date: DateModel): Array<Int> {
    var englishCalendar = CivilCalendar()
    englishCalendar.year = date.Year.toInt()
    englishCalendar.month = date.Month
    englishCalendar.dayOfMonth = date.Day.toInt()
    val persianCalendar = englishCalendar.toPersian()

    return arrayOf(persianCalendar.year, persianCalendar.month, persianCalendar.dayOfMonth)
}



