/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui.button.type;

import com.illuzionzstudios.mist.compatibility.UMaterial;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.ui.render.ItemCreator;
import lombok.*;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * A button that returns to a previous/parent {@link UserInterface}
 */
@RequiredArgsConstructor
@AllArgsConstructor
public final class ReturnBackButton extends Button {

    /**
     * Material for this button
     */
    @Getter
    @Setter
    private static UMaterial material = UMaterial.OAK_DOOR;

    /**
     * The title of this button
     */
    @Getter
    @Setter
    private static String title = "&4&lReturn";

    /**
     * The lore of this button
     */
    @Getter
    @Setter
    private static List<String> lore = Arrays.asList("", "Return back.");

    /**
     * The parent {@link UserInterface}
     */
    @NonNull
    private final UserInterface parentInterface;

    /**
     * Make a new instance of the {@link UserInterface} when showing
     */
    private boolean makeNewInstance = false;

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(material).name(title).lores(lore).build().makeUIItem();
    }

    /**
     * Open the parent interface
     */
    @Override
    public ButtonListener getListener() {
        return ((player, ui, type) -> {
            if (makeNewInstance)
                parentInterface.newInstance().show(player);
            else
                parentInterface.show(player);
        });
    }
}
