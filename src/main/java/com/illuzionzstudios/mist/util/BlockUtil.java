package com.illuzionzstudios.mist.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil {

    public static List<Block> getBlocks(final Location pos1, final Location pos2)
    {
        if(pos1.getWorld() != pos2.getWorld())
            return null;
        World world = pos1.getWorld();
        List<Block> blocks = new ArrayList<>();
        int x1 = pos1.getBlockX();
        int y1 = pos1.getBlockY();
        int z1 = pos1.getBlockZ();

        int x2 = pos2.getBlockX();
        int y2 = pos2.getBlockY();
        int z2 = pos2.getBlockZ();

        int lowestX = Math.min(x1, x2);
        int lowestY = Math.min(y1, y2);
        int lowestZ = Math.min(z1, z2);

        int highestX = lowestX == x1 ? x2 : x1;
        int highestY = lowestX == y1 ? y2 : y1;
        int highestZ = lowestX == z1 ? z2 : z1;

        for(int x = lowestX; x <= highestX; x++)
            for(int y = lowestY; x <= highestY; y++)
                for(int z = lowestZ; x <= highestZ; z++)
                    blocks.add(world.getBlockAt(x, y, z));
        return blocks;
    }

}
