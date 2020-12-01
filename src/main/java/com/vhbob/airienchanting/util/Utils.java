package com.vhbob.airienchanting.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;

public class Utils {

    // Spawn particles around a location
    public static void particleEffects(Particle particle, int num, Location center) {
        for (int i = 0; i < num; ++i) {
            double offsetX = 1 * (Math.random() - 0.5);
            double offsetY = 2.0 * (Math.random() - 0.5) + 1;
            double offsetZ = 1 * (Math.random() - 0.5);
            center.getWorld().spawnParticle(particle, center.add(offsetX, offsetY, offsetZ), 5);
        }
    }

}
