package com.extremelyd1.world.spawn;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnFindThread extends Thread {

    /**
     * The location used to start this search with.
     */
    private final Location startLocation;

    /**
     * The spiral used to find a valid spawn.
     */
    private final Spiral spiral;
    /**
     * The max width of the spiral after which the search ends.
     */
    private final int maxSearchWidth;

    /**
     * Whether the search is done.
     */
    private boolean done;
    /**
     * The location found in the search.
     */
    private Location foundLocation;

    // fallen's fork: prevent spawning in biome without trees
    private boolean allowSpawnBiomeWithoutTree;

    public SpawnFindThread(
            Location location, int maxSearchWidth,

            // fallen's fork: prevent spawning in biome without trees
            boolean allowSpawnBiomeWithoutTree
    ) {
        this.startLocation = location;
        // Create spiral to find valid spawn
        this.spiral = new Spiral(location);
        this.maxSearchWidth = maxSearchWidth;

        // If start location is already valid, return it
        if (LocationUtil.isValidSpawnLocation(location)) {
            this.foundLocation = location;
            this.done = true;
        } else {
            this.done = false;
        }

        // fallen's fork: prevent spawning in biome without trees
        this.allowSpawnBiomeWithoutTree = allowSpawnBiomeWithoutTree;
    }

    @Override
    public void run() {
        while (!this.done) {
            // Stop searching when we reach max search width
            if (this.spiral.getCurrentWidth() >= this.maxSearchWidth) {
                Game.getLogger().info(
                        "Stopping search for spawn after spiral width of "
                                + this.maxSearchWidth
                );

                // Default to returning start location if nothing can be found
                this.foundLocation = this.startLocation;
                this.done = true;

                return;
            }

            // Advance the spiral a step and check new location
            WorldChunkCoordinate newChunkCoords = this.spiral.step();

            Location centerLocation = newChunkCoords.getCenter();
            if (!LocationUtil.isInsideWorldBorder(centerLocation)) {
                Game.getLogger().info(
                        "Stopping search for spawn after passing world border at "
                                + this.spiral.getNumIterations()
                                + " spiral iterations"
                );

                // Default to returning start location if nothing can be found
                this.foundLocation = this.startLocation;
                this.done = true;

                return;
            }

            // fallen's fork: prevent spawning in biome without trees
            boolean ok = LocationUtil.isValidSpawnBiome(newChunkCoords.getBiome());
            ok &= this.allowSpawnBiomeWithoutTree || LocationUtil.isBiomeWithTree(newChunkCoords.getBiome());

            if (ok) {
                // Now find a suitable block location inside the found chunk
                int startX = newChunkCoords.getX() * 16;
                int startZ = newChunkCoords.getZ() * 16;
                World world = newChunkCoords.getWorld();

                // Loop through coordinates within chunk
                for (int x = startX; x < startX + 16; x++) {
                    for (int z = startZ; z < startZ + 16; z++) {
                        // Get location
                        Location location = new Location(
                                world,
                                x,
                                world.getHighestBlockYAt(x, z),
                                z
                        );
                        // Check whether this location is a valid spawn
                        if (LocationUtil.isValidSpawnLocation(location)) {
                            Game.getLogger().info(String.format(
                                    "Valid spawn location found (%s, %s) after %s spiral iterations",
                                    x,
                                    z,
                                    this.spiral.getNumIterations()
                            ));

                            // Set found location and return
                            this.foundLocation = location;
                            this.done = true;

                            return;
                        }
                    }
                }
            }

            try {
                //noinspection BusyWait
                Thread.sleep(10);
            } catch (InterruptedException e) {
                this.done = true;
            }
        }
    }

    public boolean isDone() {
        return done;
    }

    public Location getFoundLocation() {
        return foundLocation;
    }
}
