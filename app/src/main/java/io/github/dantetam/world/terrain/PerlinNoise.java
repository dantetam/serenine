package io.github.dantetam.world.terrain;

import java.util.Random;

public class PerlinNoise extends BaseTerrain {

    public PerlinNoise(double seed) {
        //this.seed = seed;
        random = new Random((long) seed);
    }

    public double[][] makePerlinNoise(int width, int height, double startAmp, double startFreq, double persistence, double ampFreqRatio, int times) {
        /*double[][] a = averageTables(1.25, generateNoise(64,64,32,4), generateNoise(64,64,16,8), generateNoise(64,64,8,16), generateNoise(64,64,4,32), generateNoise(64,64,2,64));
		a = scalar(0.5,a);
		//double[][] a = generateNoise(64,64,16,32);
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a[0].length; j++)
			{
				System.out.print(Math.abs((int)a[i][j]) + "   ");
			}
			System.out.println();
		}*/
        //ArrayList<double[][]> averageLater = new ArrayList<double[][]>();
        double[][][] averageLater = new double[times][width][height];
        for (int i = 0; i < times; i++) {
            averageLater[i] = generateNoise(width, height, startAmp * Math.pow(ampFreqRatio, i), (int) (startFreq / Math.pow(ampFreqRatio, i)));
        }
        double[][] b = averageTables(persistence, averageLater);
        //double[][] b = expandInterpolate(a,3);
        b = TerrainUtil.positiveTable(b);
		/*for (int i = 0; i < b.length; i++)
		{
			for (int j = 0; j < b[0].length; j++)
			{
				System.out.print((int)b[i][j] + "   ");
			}
			System.out.println();
		}*/
        return b;
    }

    public double[][] averageTables(double ratio, double[][][] a) {
        double[][] returnThis = new double[a[0].length][a[0][0].length];
        for (int tNum = 0; tNum < a.length; tNum++) {
            for (int i = 0; i < a[0].length; i++) {
                for (int j = 0; j < a[0][0].length; j++) {
                    returnThis[i][j] += a[tNum][i][j] * (1 / Math.pow(ratio, tNum - 1));
                }
            }
        }
        return returnThis;
    }

	/*public double[][] averageTables(double ratio, double[][]... a)
	{
		double[][] returnThis = new double[a[0].length][a[0][0].length];
		for (int tNum = 0; tNum < a.length; tNum++)
		{
			//System.out.println("kek");
			for (int i = 0; i < a[0].length; i++)
			{
				for (int j = 0; j < a[0][0].length; j++)
				{
					returnThis[i][j] += a[tNum][i][j]*(1/Math.pow(ratio, tNum-1));
				}
			}
		}
		return returnThis;
	}*/

    public double[][] generateNoise(int width, int height, double amp, int freq) {
        double[][] returnThis = new double[width][height];
        for (int i = 0; i < width; i += width / freq) {
            for (int j = 0; j < height; j += height / freq) {
                fillPatch(returnThis, i, j, width / freq, height / freq, random.nextDouble() * amp - amp / 2);
            }
        }
        return returnThis;
    }

    public void fillPatch(double[][] arr, int a, int b, int w, int h, double number) {
        for (int i = a; i < a + w; i++) {
            for (int j = b; j < b + w; j++) {
                if (i < arr.length && j < arr[0].length)
                    arr[i][j] = number;
            }
        }
    }

    //TODO: Fix this method
    //public double[][] expandInterpolate(double[][] a, int n)

    public double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public double bInter(double[] distances, double[] values) {
        //System.out.println(values[0]);
        if (distances.length != values.length) return -1;
        double returnThis = 0;
        double sum = 0;
        for (int i = 0; i < distances.length; i++) sum += distances[i];
        for (int i = 0; i < distances.length; i++) {
            returnThis += values[i] * (sum - distances[i]);
        }
        returnThis /= sum;
        //System.out.println(returnThis);
        return returnThis;
    }

    public double[][] generate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double[][] generate(double[] a) {
        //for (int i = 0; i < a.length; i++)
        //System.out.println(a[i]);
        double[][] source = makePerlinNoise((int) a[0], (int) a[1], a[2], a[3], a[4], a[5], (int) a[6]);
        //double[][] newSource = PerlinNoise.recurInter(source,2,nDiv/4);
        source = TerrainUtil.expand(TerrainUtil.expand(source, a[7] / 2), a[7]);
        if (a.length == 9) {
            source = TerrainUtil.cutoff(source, a[8]);
        }
        return source;
    }

}
