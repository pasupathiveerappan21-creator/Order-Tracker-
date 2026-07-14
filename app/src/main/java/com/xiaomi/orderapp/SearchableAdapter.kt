package com.xiaomi.orderapp

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

/**
 * ArrayAdapter that matches anywhere in the text (not just the start),
 * so typing part of a product name shows everything related to it.
 */
class SearchableAdapter(context: Context, private val allItems: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, allItems.toMutableList()) {

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val query = constraint?.toString()?.lowercase() ?: ""
                val matches = if (query.isEmpty()) {
                    allItems
                } else {
                    allItems.filter { it.lowercase().contains(query) }
                }
                results.values = matches
                results.count = matches.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                clear()
                addAll(results.values as List<String>)
                notifyDataSetChanged()
            }
        }
    }
    }
