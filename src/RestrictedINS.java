public class RestrictedINS extends INS {
    private int[] boundaryTL;
    private int[] boundaryBR;

    public RestrictedINS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant, int[] boudaryTL, int[] boundaryBR) {
        super(id, name, field, debug, pos, r, c, restaurant);

        this.boundaryBR = boundaryBR;
        this.boundaryTL = boudaryTL;
    }

    private boolean inBoundary(int[] coords) {
        int relR = coords[0] - boundaryTL[0];
        int relC = coords[1] - boundaryTL[1];

        if (relR < 0 || relC < 0)
            return false;

        if (relR > boundaryBR[0] || relC > boundaryBR[1])
            return false;

        return true;
    }

    @Override
    protected boolean thirdPartyCheck() {
        return inBoundary(orderOnHold);
    }
}