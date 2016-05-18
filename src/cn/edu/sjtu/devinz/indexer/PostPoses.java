package cn.edu.sjtu.devinz.indexer;

import java.util.StringTokenizer;

class PostPoses {

    /** PostPos = 5-bit partition # + 27-bit slot # */

    public static final int SIZE_LEN = 4;
    public static final int RADIX = 3;
    public static final int BLOCK = 128;
    public static final int UNIT = (RADIX-1)*BLOCK;

    public static int getSlotVolume(int partNO) {
        return UNIT*getVolHelp(partNO) + SIZE_LEN;
    }

    private static int getVolHelp(int partNO) {
        if (0 == partNO) {
            return 1;
        } else {
            int tmp = getVolHelp(partNO/2);

            if (0 == partNO%2) {
                return tmp * tmp;
            } else {
                return RADIX * tmp * tmp;
            }
        }
    }

    public static int encode(String filename, int slotNO) {
        StringTokenizer toks = new StringTokenizer(filename, "/");
        while (toks.countTokens() > 1) {
            toks.nextToken();
        }
        filename = toks.nextToken();
        int dot = filename.indexOf('.');
        int partNO = Integer.valueOf(filename.substring(0, dot));

        if (partNO<0 || partNO>=32 || Zones.encode(filename.substring(dot+1))<0) {
            throw new IllegalArgumentException("Invalid Inverted-File Name.");
        }
        return (partNO<<27)+slotNO;
    }

    public static int getPartNO(int postPos) {
        return (postPos & (-(1<<27)))>>>27;
    }

    public static int getSlotNO(int postPos) {
        return postPos & ((1<<27)-1);
    }

    public static void main(String[] args) {
        for (int i=0; i<5; i++) {
            System.out.println("slotVolume("+i+") = "+getSlotVolume(i));
        }
        System.out.println();

        java.util.Random rand = new java.util.Random();
        for (int test=0; test<100; test++) {
            int partNO = rand.nextInt(16);
            int fieldCode = rand.nextInt(Zones.NUM_OF_ZONES);
            int slotNO = rand.nextInt(1<<24);
            int code = encode(Integer.toString(partNO)+"."+Zones.decode(fieldCode), slotNO);

            if (code != (partNO<<27) + slotNO) {
                throw new RuntimeException("Failure.");
            }
        }
        System.out.println("All tests OK.");
    }

}
