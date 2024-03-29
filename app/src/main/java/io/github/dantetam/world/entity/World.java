package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.dantetam.opstrykontest.WorldParams;
import io.github.dantetam.opstrykontest.WorldSystem;
import io.github.dantetam.world.factory.TileFactory;

/**
 * Created by Dante on 6/13/2016.
 */
public class World {

    //private QuadTree<Tile, int[]> tiles;
    //private WorldTree tree;

    public WorldSystem worldSystem;

    protected Tile[][] hexes;
    public int arrayLengthX, arrayLengthZ;
    public int totalX, totalZ;
    private int numHexes = -1;

    private List<Tile> validTiles;

    private List<Clan> clans;

    private HashMap<Tile, Clan> tileOwnerHashMap;
    //private HashMap<Tile, Influence> tileInfluenceHashMap;

    public List<Tile> clanTerritoriesUpdate;

    //x represents height, z represents length
    public World(WorldParams params) {
        //tree = new WorldTree();
        int q = params.len1, r = params.len2;
        hexes = new Tile[r][q + r/2];
        validTiles = new ArrayList<Tile>();
        this.totalX = q; this.totalZ = r;
        this.arrayLengthX = r; this.arrayLengthZ = q + r/2;

        clans = new ArrayList<>();
        clanTerritoriesUpdate = new ArrayList<>();
    }

    public void init(int[][] biomes, int[][] terrains, int[][] elevations) {
        int startingZ = arrayLengthZ - 1;
        numHexes = 0;
        for (int x = 0; x < arrayLengthX; x++) {
            for (int z = startingZ; z >= startingZ - this.totalZ; z--) {
                Tile.Biome biome = Tile.Biome.fromInt(biomes[x][z]);
                Tile.Terrain terrain;
                if (biome == Tile.Biome.SEA) {
                    //Normalize to values 0 and 1, shallow_sea and deep_sea
                    int typeOfSea = Math.round(biome.type / Tile.Biome.numBiomes);
                    terrain = Tile.Terrain.fromInt(typeOfSea);

                    elevations[x][z] = 0;
                } else {
                    if (terrains[x][z] <= 1) {
                        terrains[x][z] = (int)(Math.random() * (Tile.Terrain.numTerrains - Tile.Terrain.numSeaTerrains)) + 2;
                    }
                    terrain = Tile.Terrain.fromInt(terrains[x][z]);
                }
                hexes[x][z] = TileFactory.newTile(biome, terrain, null, this, x, z);

                hexes[x][z].resources = new ArrayList<Item>();
                //tile.resources.add(Tile.Resource.fromInt(resources[r][c]));
                //tile.resources.add(resources[r][c]);
                hexes[x][z].elevation = elevations[x][z];

                validTiles.add(hexes[x][z]);
                numHexes++;
            }
            if (x % 2 == 1) {
                startingZ--;
            }
        }

        /*for (int i = 0; i < hexes.length; i++) {
            for (int j = 0; j < hexes[0].length; j++) {
                String stringy = hexes[i][j] != null ? "X" : "-";
                System.out.print(stringy + " ");
            }
            System.out.println();
        }*/
    }

    public List<Clan> getClans() {
        return clans;
    }
    public void initClans(List<Clan> c) {
        clans = c;
        tileOwnerHashMap = new HashMap<>();
        /*tileInfluenceHashMap = new HashMap<>();
        for (Tile t: getAllValidTiles()) {
            tileInfluenceHashMap.put(t, new Influence(clans));
        }*/
    }

    public void setTileOwner(Tile t, Clan c) {
        tileOwnerHashMap.put(t, c);
        if (!clanTerritoriesUpdate.contains(t)) clanTerritoriesUpdate.add(t);
    }
    public Clan getTileOwner(Tile t) {
        return tileOwnerHashMap.get(t);
    }
    /*public void addTileInfluence(Tile t, Clan c, int influenceNum) {
        Influence influence = tileInfluenceHashMap.get(t);
        influence.addClanInfluence(c, influenceNum);
        if (!clanTerritoriesUpdate.contains(t)) clanTerritoriesUpdate.add(t);
    }
    public Clan getTileInfluence(Tile tile) {
        Clan owner = tileOwnerHashMap.get(tile);
        return owner != null ? owner : tileInfluenceHashMap.get(tile).influencingClan();
    }*/

    public Object[] aggregateOwners(List<Tile> src) {
        List<Tile> tiles = new ArrayList<>();
        for (Tile t: src) {
            tiles.add(t);
        }
        HashMap<Clan, List<Tile>> owners = new HashMap<>(), influencers = new HashMap<>();
        for (Clan c: clans) {
            owners.put(c, new ArrayList<Tile>());
            influencers.put(c, new ArrayList<Tile>());
        }
        for (int i = tiles.size() - 1; i >= 0; i--) {
            Tile t = tiles.get(i);
            Clan owner = getTileOwner(t);
            //Clan influence = getTileInfluence(t);
            if (owner != null) {
                owners.get(owner).add(t);
                tiles.remove(i);
            }
            /*else if (influence != null) {
                influencers.get(influence).add(t);
                tiles.remove(i);
            }*/
        }
        /*System.out.println("Tiles total: " + getAllValidTiles().size());
        for (Clan c: clans) {
            System.out.println("Tiles owned by clan " + c.name + ": " + owners.get(c).size());
            System.out.println("Tiles influenced by clan " + c.name + ": " + influencers.get(c).size());
        }*/
        return new Object[]{owners, influencers, tiles};
    }
    /*public HashMap<Clan, List<Tile>> aggregateInfluence(List<Tile> tiles) {
        HashMap<Clan, List<Tile>> result = new HashMap<>();
        for (Clan c: clans) {
            result.put(c, new ArrayList<Tile>());
        }
        for (Tile t: tiles) {
            Clan owner = getTileInfluence(t);
            if (owner != null) {
                result.get(owner).add(t);
            }
        }
        return result;
    }*/

    public boolean isCoastal(Tile tile) {
        List<Tile> neighbors = neighbors(tile);
        for (Tile t: neighbors) {
            if (t.biome == Tile.Biome.SEA) {
                return true;
            }
        }
        return false;
    }

    public float buildingModifier(Tile tile, Clan builder) {
        if (builder.equals(tileOwnerHashMap.get(tile))) {
            return 0.7f;
        }
        /*else if (builder.equals(tileInfluenceHashMap.get(tile).influencingClan())) {
            return 0.85f;
        }
        else {
            float clanInfluencePercentage = tileInfluenceHashMap.get(tile).percentInfluenceOfClan(builder);
            //clanInfluencePercentage /= 0.5f;
            //float extraTime = 0.5f - clanInfluencePercentage;
            return 1.5f - clanInfluencePercentage;
        }*/
        return 1f;
    }

    public Tile getTile(int r, int c) {
        if (r < 0 || c < 0 || r >= hexes.length || c >= hexes[0].length) {
            //throw new IllegalArgumentException("Out of bounds or degenerate grid");
            return null;
        }
        return hexes[r][c];
    }

    public List<Tile> getAllValidTiles() {
        /*List<Tile> tiles = new ArrayList<Tile>();
        int startingZ = arrayLengthZ - 1;
        for (int x = 0; x < arrayLengthX; x++) {
            for (int z = startingZ; z >= startingZ - arrayLengthX; z--) {
                Tile tile = hexes[x][z];
                if (tile != null) tiles.add(tile);
            }
            if (x % 2 == 1) {
                startingZ--;
            }
        }
        return tiles;*/
        return validTiles;
    }

    private List<Tile> landTiles = null;
    public List<Tile> getAllLandTiles() {
        if (landTiles == null) {
            landTiles = new ArrayList<>();
            for (Tile tile: getAllValidTiles()) {
                if (tile.biome != Tile.Biome.SEA) {
                    landTiles.add(tile);
                }
            }
        }
        return landTiles;
    }

    public List<Tile> getValidExpansionTiles(City c) {
        List<Tile> expansion = new ArrayList<>();
        for (Tile cityTile: c.cityTiles) {
            for (Tile neighbor: cityTile.neighbors()) {
                if (getTileOwner(neighbor) == null) {
                    expansion.add(neighbor);
                }
            }
        }
        return expansion;
    }

    //Note that the order doesn't matter here, but other classes will rely on this.
    public static final int[][] neighborDirections = {
            {0, -1}, {-1, 0}, {-1, 1},
            {0, 1}, {1, 0}, {1, -1}
    };
    public List<Tile> neighbors(Tile t) {
        List<Tile> temp = new ArrayList<Tile>();
        for (int i = 0; i < neighborDirections.length; i++) {
            Tile candidate = getTile(t.q + neighborDirections[i][0], t.r + neighborDirections[i][1]);
            if (candidate != null) temp.add(candidate);
        }
        return temp;
    }

    public Tile randomNeighbor(Tile t) {
        List<Tile> neighbors = neighbors(t);
        if (neighbors.size() == 0) return null;
        return neighbors.get((int)(Math.random()*neighbors.size()));
    }

    /*public List<Tile> getRotationsInOrder(Tile t, int radius) {

    }*/

    /**
     * Generalize with a recursive traversal.
     */
    public Collection<Tile> getRing(Tile t, int radius) {
        Set<Tile> rings = new HashSet<>();
        if (radius == 1) {
            Collection<Tile> tiles = neighbors(t);
            tiles.add(t);
            return tiles;
        }
        rings.add(t);
        if (radius > 0) {
            for (Tile neighbor: neighbors(t)) {
                Collection<Tile> neighborRing = getRing(neighbor, radius - 1);
                for (Tile neighborRingTile: neighborRing) {
                    rings.add(neighborRingTile);
                }
            }
        }
        return rings;
    }

    public boolean[] neighborsAreDifferent(Tile t) {
        boolean[] neighbors = new boolean[neighborDirections.length];
        for (int i = 0; i < neighborDirections.length; i++) {
            int[] offset = neighborDirections[i];
            Tile neighborTile = getTile(t.q + offset[0], t.r + offset[1]);
            neighbors[i] = false;
            if (neighborTile != null) {
                //neighbors[i] = neighborTile == null || !getTileOwner(t).equals(getTileOwner(neighborTile));
                if (getTileOwner(t) != null) {
                    neighbors[i] = !getTileOwner(t).equals(getTileOwner(neighborTile));
                    //neighbors[i] = neighbors[i] || !getTileOwner(t).equals(getTileInfluence(neighborTile));
                }
                else {
                    /*if (getTileInfluence(t) != null) {
                        if (getTileOwner(neighborTile) == null && getTileInfluence(neighborTile) == null) {
                            neighbors[i] = true;
                        }
                        else if (getTileOwner(neighborTile) != null) {
                            neighbors[i] = !getTileInfluence(t).equals(getTileOwner(neighborTile));
                        }
                        else {
                            neighbors[i] = !getTileInfluence(t).equals(getTileInfluence(neighborTile));
                        }
                    }
                    else {
                        //Do nothing
                    }*/
                }
            }
            else {
                neighbors[i] = true;
            }
        }
        return neighbors;
    }

    /*public int[] neighborsElevations(Tile t) {
        int[] neighbors = new int[neighborDirections.length];
        for (int i = 0; i < neighborDirections.length; i++) {
            int[] offset = neighborDirections[i];
            Tile neighborTile = getTile(t.q + offset[0], t.r + offset[1]);
            int diff = t.elevation - neighborTile.elevation;
            if (diff <= 1) {
                if (neighborTile.biome == Tile.Biome.SEA) {
                    neighbors[i] = R.raw.terrain_shore;
                }
                else {
                    neighbors[i] = -1;
                }
            }
            else if (diff <= 3) {
                neighbors[i] = R.raw.terrain_stairs;
            }
            else if (neighborTile.biome == Tile.Biome.SEA) {
                neighbors[i] = R.raw.terrain_sea_cliff;
            }
            else if (diff <= 3) {
                neighbors[i] = R.raw.terrain_hills;
            }
            else {
                neighbors[i] = R.raw.terrain_land_cliff;
            }
        }
        return neighbors;
    }*/

    /*public Set<Tile> getRingTwo(Tile t, int radius) {
        if (radius < 0) radius = -radius;
        if (radius == 0) {
            Set<Tile> neighbors = new HashSet<>();
            neighbors.add(t);
            return neighbors;
        }
        List<Tile> neighbors = neighbors(t);
        HashMap<Tile, Boolean> stored = new HashMap<>();
        for (Tile neighbor: neighbors) {
            for (Tile neighborNeighbor: neighbors(neighbor)) {
                if (!stored.containsKey(neighborNeighbor)) {
                    stored.put(neighborNeighbor, true);
                }
            }
        }
        return stored.keySet();
    }*/

    public int getNumHexes() {
        if (numHexes == -1) {
            throw new RuntimeException("World has not been initialized");
        }
        return numHexes;
    }

    public Tile[][] returnTilesForChunking() {
        return hexes;
    }

}
