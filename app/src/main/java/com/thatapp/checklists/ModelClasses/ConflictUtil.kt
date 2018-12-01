package com.thatapp.checklists.ModelClasses


import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Arrays

internal object ConflictUtil {
    /**
     * Performs a three-way merge of three sets of "grocery items" into one set.
     *
     * @param baseStr     Items local modifications are based on.
     * @param currentStr  Items currently on the server.
     * @param modifiedStr Locally modified items.
     * @return Items merged from three sets of items provided.
     */
    fun resolveConflict(baseStr: String, currentStr: String, modifiedStr: String): String {
        val baseItems = Arrays.asList(*baseStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val currentItems = Arrays.asList(*currentStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val modifiedItems = Arrays.asList(*modifiedStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val allItems = ArrayList<String>()

        // Add unique items to allItems.
        allItems.addAll(baseItems)
        for (item in currentItems) {
            if (!allItems.contains(item)) {
                allItems.add(item)
            }
        }
        for (item in modifiedItems) {
            if (!allItems.contains(item)) {
                allItems.add(item)
            }
        }

        // Remove items that were removed from currentItems or modifiedItems.
        val iter = allItems.iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            if (baseItems.contains(item) && (!currentItems.contains(item) || !modifiedItems.contains(item))) {
                iter.remove()
            }
        }
        val stringBuilder = StringBuilder()
        for (item in allItems) {
            stringBuilder.append(item)
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    /**
     * Gets String from InputStream.
     *
     * @param is InputStream used to read into String.
     * @return String resulting from reading is.
     */
    fun getStringFromInputStream(`is`:InputStream):String {
        val sb = StringBuilder()
        val line:String?=null
        try
        {
            BufferedReader(InputStreamReader(`is`)).use { br-> while ((br.readLine()) != null) {
                sb.append(line).append("\n")
            } }
        }
        catch (e:IOException) {
            throw RuntimeException("Unable to read string content.")
        }
        return sb.toString()
    }
}