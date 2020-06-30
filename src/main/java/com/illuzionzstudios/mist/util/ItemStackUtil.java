/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class to deal with {@link org.bukkit.inventory.ItemStack}.
 * This could be parsing the data or dealing with NBT data on the item
 */
public final class ItemStackUtil {

    /**
     * Serialize a {@link ItemStack} into a string blob
     *
     * @param itemStack ItemStack to serialize
     * @return ItemStack as a string blob
     */
    public static String serialize(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return config.saveToString();
    }

    /**
     * Deserialize a string blob into a {@link ItemStack}
     * See {@link #serialize(ItemStack)}
     *
     * @param stringBlob String blob to convert to {@link ItemStack}
     * @return Formed {@link ItemStack}. {@code null} if didn't deserialize properly
     */
    public static ItemStack deserialize(String stringBlob) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(stringBlob);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }

}
