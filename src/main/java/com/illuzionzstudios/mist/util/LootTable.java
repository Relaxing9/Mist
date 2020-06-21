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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Contains items that can be picked based on weights
 */
public class LootTable<T> {

    /**
     * Random instance for chances
     */
    private Random random = new Random();

    /**
     * Stored rewards in our table
     */
    private List<Pair<T, Double>> lootTable = new LinkedList<>();

    /**
     * Total weight of rewards used for picking
     */
    private double totalWeight;

    /**
     * Add new loot with weight
     *
     * @param type   The type of loot based on LootTable
     * @param weight The weight as a double
     */
    public void addLoot(T type, double weight) {
        lootTable.add(new Pair<>(type, weight));
        totalWeight += weight;
    }

    /**
     * Pick random item from loot table based on weight
     */
    public T pick() {
        double currentItemUpperBound = 0;

        double nextValue = (totalWeight - 0) * random.nextDouble();
        for (Pair<T, Double> itemAndWeight : lootTable) {
            currentItemUpperBound += itemAndWeight.getValue();
            if (nextValue < currentItemUpperBound)
                return itemAndWeight.getKey();
        }

        return lootTable.get(lootTable.size() - 1).getKey();
    }

    /**
     * Clears all current loot in the table
     */
    public void clear() {
        this.lootTable.clear();
    }

}
