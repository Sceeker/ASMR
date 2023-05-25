public class RestrictedINS extends INS {
    private int[] boundaryTL;
    private int[] boundaryBR;

    public RestrictedINS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant, int[] boudaryTL, int[] boundaryBR) {
        super(id, name, field, debug, pos, r, c, restaurant);

        this.boundaryBR = boundaryBR;
        this.boundaryTL = boudaryTL;
    }

    @Override
    protected boolean thirdPartyCheck(int[] test) {
        int relR = test[0] - boundaryTL[0];
        int relC = test[1] - boundaryTL[1];

        if (relR < 0 || relC < 0)
            return false;

        if (test[0] > boundaryBR[0] || test[1] > boundaryBR[1])
            return false;

        return true;
    }
}