package com.illuzionzstudios.mist.requirement

import com.illuzionzstudios.mist.config.ConfigSection
import com.illuzionzstudios.mist.config.serialization.loader.type.YamlSectionLoader

/**
 * Load a requirement from a config section
 */
class PlayerRequirementLoader(section: ConfigSection): YamlSectionLoader<PlayerRequirement>(section) {

    override fun save(): Boolean {
        TODO("Not yet implemented")
    }

    override fun loadObject(file: ConfigSection?): PlayerRequirement = PlayerRequirement(RequirementType.getFilter(file?.getString("type") ?: "permission"), file?.getString("value"), file?.getString("output"))
}