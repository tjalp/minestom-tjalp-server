package net.tjalp.peach.peel.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Helper class for using Gson functionality
 * within peach.
 *
 * Copyright 2019-2021 (c) Exodius Studios. All Rights Reserved.
 *
 * @author Jøøls
 */
object GsonHelper {

	/** The internal Gson builder */
	private val globalBuilder: GsonBuilder = GsonBuilder()
	private var gsonCache: Gson? = null
	private val gsonPrettyCache: Gson = GsonBuilder().setPrettyPrinting().create()

	/**
	 * Returns the builder instance for the global Gson parser
	 *
	 * @return The global GsonBuilder
	 */
	fun modify() : GsonBuilder {
		gsonCache = null
		return globalBuilder
	}

	/**
	 * Returns the global Gson instance. Once this method is
	 * called, all future modifications to the global gson
	 * will be prevented.
	 *
	 * @return Gson
	 */
	fun global() : Gson {
		return gsonCache ?: globalBuilder.create().also {
			gsonCache = it
		}
	}

	/**
	 * Returns the global pretty Gson instance.
	 *
	 * @return Pretty Gson
	 */
	fun pretty() : Gson {
		return gsonPrettyCache
	}

	private val pathRegex = Regex("^[a-zA-Z_.]+$")

	/**
	 * Resolve the given access path on the supplied JsonObject
	 *
	 * @param instance The json object
	 * @return The resolved element
	 */
	fun resolve(instance: JsonObject?, path: String) : JsonElement? {
		if(!pathRegex.matches(path)) {
			throw IllegalArgumentException("Path $path does not match format $pathRegex")
		} else if(instance == null) {
			return null
		}

		val segments = path.split(".")
		var prev: JsonElement = instance

		for(seg in segments) {
			prev = prev.asJsonObject[seg] ?: return null
		}

		return prev
	}
}
