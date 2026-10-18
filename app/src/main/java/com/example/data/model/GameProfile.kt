package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a game-specific preparation profile.
 */
data class GameProfile(
    val id: String,
    val name: String,
    val gamePackageName: String? = null,
    val selectedPackages: Set<String> = emptySet(),
    val protectedPackages: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("gamePackageName", gamePackageName ?: "")
            put("selectedPackages", JSONArray(selectedPackages.toList()))
            put("protectedPackages", JSONArray(protectedPackages.toList()))
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): GameProfile {
            val selectedList = mutableSetOf<String>()
            val selectedArr = json.optJSONArray("selectedPackages")
            if (selectedArr != null) {
                for (i in 0 until selectedArr.length()) {
                    selectedList.add(selectedArr.getString(i))
                }
            }

            val protectedList = mutableSetOf<String>()
            val protectedArr = json.optJSONArray("protectedPackages")
            if (protectedArr != null) {
                for (i in 0 until protectedArr.length()) {
                    protectedList.add(protectedArr.getString(i))
                }
            }

            val gamePkg = json.optString("gamePackageName").takeIf { it.isNotBlank() }

            return GameProfile(
                id = json.getString("id"),
                name = json.getString("name"),
                gamePackageName = gamePkg,
                selectedPackages = selectedList,
                protectedPackages = protectedList,
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
