/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.config.json;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads all .json files from a directory
 */
public class JsonDirectoryLoader {

    /**
     * All file loaders for .json files in directory
     */
    @Getter
    private final List<JsonFileLoader> loaders;

    /**
     * @param directory to load from
     */
    public JsonDirectoryLoader(String directory) {
        loaders = new ArrayList<>();

        // Reward directory
        File dir = new File(SpigotPlugin.getInstance().getDataFolder().getPath() + File.separator + directory);

        // Ensure exists
        if (dir.listFiles() == null || !dir.exists()) return;

        // Go through files
        for (File file : dir.listFiles()) {
            // Get name without extension
            String name = file.getName().split("\\.")[0];

            // Only load .json files
            if (file.getName().split("\\.")[1].equalsIgnoreCase("json"))
                // Add to cache
                loaders.add(new JsonFileLoader(name, directory));
        }
    }

}
