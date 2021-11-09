package net.tjalp.peach.peel.config

import net.tjalp.peach.peel.exception.FailedOperationException
import net.tjalp.peach.peel.util.GsonHelper
import java.io.File

/**
 * Each instance of [JsonConfig] represents
 *
 * Copyright 2019-2021 (c) Exodius Studios. All Rights Reserved.
 *
 * @author Jøøls
 */
class JsonConfig<T : Configurable>(private val file: File, private val type: Class<T>, create: Boolean = true) {

    /** The gson instance **/
    private val gson = GsonHelper.global()

    /** The data contained within the config **/
    lateinit var data: T
        private set

    init {
        if (file.exists()) {
            load()
        } else {
            try {
                data = type.getConstructor().newInstance()
            } catch (err: Exception) {
                throw FailedOperationException("Failed to instantiate config", err)
            }

            if (create) {
                save()
            }
        }
    }

    /**
     * Reload the config from disk and replace the
     * current config.
     */
    fun load() {
        file.bufferedReader().use {
            data = gson.fromJson(it, type)
            data.onLoad()
        }
    }

    /**
     * Save the current config to disk
     *
     * @param output The output file, null for the input file
     */
    fun save(output: File? = null) {
        val json = gson.toJson(data)
        val formatted = GsonHelper.pretty().toJson(json)
        val outFile = output ?: file

        outFile.createNewFile()
        outFile.writeText(formatted)

        data.onSave()
    }

}