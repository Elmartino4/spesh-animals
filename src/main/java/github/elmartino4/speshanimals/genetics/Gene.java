package github.elmartino4.speshanimals.genetics;

import net.minecraft.server.world.ServerWorld;

public class Gene {
    public static final double MIN_DIFFERENCE = 1;
    private final int id;
    private double[] value = null;
    private final int parameters;
    private long worldSeed;
    private OpenSimplexNoise noise;

    /*public static void setSeed(){
        noise = new OpenSimplexNoise(0);
    }*/

    public Gene(int params, int offset){
        id = offset;
        parameters = params;
        noise = new OpenSimplexNoise(0);
    }

    public double getValue(ServerWorld world, float dna){
        return getValues(world, dna)[0];
    }

    public double[] getValues(ServerWorld world, float dna){
        if(world.getSeed() != worldSeed){
            worldSeed = world.getSeed();
            noise = new OpenSimplexNoise((worldSeed % 32767) / 32);
        }

        value = new double[parameters];
        for (int i = 0; i < parameters; i++) {
            double eval = noise.eval((i + id) * MIN_DIFFERENCE,  dna);
            //System.out.println("noise.eval(" + (i + id) * MIN_DIFFERENCE + ",  " + dna + ") = " + eval);
            value[i] = eval;
        }

        return value.clone();
    }
}