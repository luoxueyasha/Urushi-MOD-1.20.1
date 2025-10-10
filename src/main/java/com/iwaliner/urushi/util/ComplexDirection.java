package com.iwaliner.urushi.util;

/*
    ComplexDirection follows this diagram below:

    Bits | Properties
    —————┼—————————
     7   | If bit7 is zero, then ComplexDirection is NULL. Default value for NULL is 0(zero).
    —————┼—————————
     6~4 | Bit6~bit4 stands for the vertical tilt angle on Y-axis.
    —————┼—————————
     3~0 | Bit3~bit0 stands for the horizontal angle on X-Z platform.

     * Especially, as bit3 is for N&S, we could treat them as inverted directions in Y-axis;
                   and as bit2 is for W&E, we could save direction infos in Y-axis.

     For example:
      N                      = 1000 0000 = 0x80
      N_NE(N+22.5 CW on X-Z) = 1000 0001 = 0x81
      NE(N+45 CW on X-Z)     = 1000 0010 = 0x82
      E                      = 1000 0100 = 0x84
      S                      = 1000 1000 = 0x88
      W                      = 1000 1100 = 0x8C

      N_UN(N+22.5 CW on Y)   = 1001 0000 = 0x90
      UN(N+45 CW on Y)       = 1010 0000 = 0xA0

      U_NSdir                = 1100 0000 = 0xC0 (Faces Up and the mirror goes North-South, as called "A1" in former versions)
      U_WEdir                = 1100 0100 = 0xC4 (Faces Up and the mirror goes West-East, as called "A2" in former versions)
      D_NSdir                = 1100 1000 = 0xC8 (Faces Down and the mirror goes North-South, as called "B1" in former versions)
      D_WEdir                = 1100 1100 = 0xCC (Faces Down and the mirror goes West-East, as called "B2" in former versions)

 */


public enum ComplexDirection {
    FAIL    ((byte) 0x00), // 0000 0000

    N       ((byte) 0x80), // 1000 0000
    N_NE    ((byte) 0x81), // 1000 0001
    NE      ((byte) 0x82), // 1000 0010
    E_NE    ((byte) 0x83), // 1000 0011
    E       ((byte) 0x84), // 1000 0100
    E_SE    ((byte) 0x85), // 1000 0101
    SE      ((byte) 0x86), // 1000 0110
    S_SE    ((byte) 0x87), // 1000 0111
    S       ((byte) 0x88), // 1000 1000
    S_SW    ((byte) 0x89), // 1000 1001
    SW      ((byte) 0x8A), // 1000 1010
    W_SW    ((byte) 0x8B), // 1000 1011
    W       ((byte) 0x8C), // 1000 1100
    W_NW    ((byte) 0x8D), // 1000 1101
    NW      ((byte) 0x8E), // 1000 1110
    N_NW    ((byte) 0x8F), // 1000 1111

    N_UN    ((byte) 0x90), // 1001 0000
    UN      ((byte) 0xA0), // 1010 0000
    U_UN    ((byte) 0xB0), // 1011 0000
    U_NSd   ((byte) 0xC0), // 1100 0000 Facing Up, and the mirror goes North-South
    U_US    ((byte) 0xD0), // 1101 0000
    US      ((byte) 0xE0), // 1110 0000
    S_US    ((byte) 0xF0), // 1111 0000

    E_UE    ((byte) 0x94), // 1001 0100
    UE      ((byte) 0xA4), // 1010 0100
    U_UE    ((byte) 0xB4), // 1011 0100
    U_WEd   ((byte) 0xC4), // 1100 0100 Facing Up, and the mirror goes East-West
    U_UW    ((byte) 0xD4), // 1101 0100 Up by Up West
    UW      ((byte) 0xE4), // 1110 0100 Up West
    W_UW    ((byte) 0xF4), // 1111 0100 West by Up West

    N_DN    ((byte) 0xF8), // 1111 1000 <-> W_UW
    DN      ((byte) 0xE8), // 1110 1000 <-> UW
    D_DN    ((byte) 0xD8), // 1101 1000 <-> U_UW
    D_NSd   ((byte) 0xC8), // 1100 1000 <-> U_NSd
    D_DS    ((byte) 0xB8), // 1011 1000 <-> U_UN
    DS      ((byte) 0xA8), // 1010 1000 <-> UN
    S_DS    ((byte) 0x98), // 1001 1000 <-> N_UN

    E_DE    ((byte) 0xFC), // 1111 1100 <-> W_UW
    DE      ((byte) 0xEC), // 1110 1100 <-> UW
    D_DE    ((byte) 0xDC), // 1101 1100 <-> U_UW
    D_WEd   ((byte) 0xCC), // 1100 1100 <-> U_WEd
    W_DW    ((byte) 0xBC), // 1011 1100 <-> E_UE
    DW      ((byte) 0xAC), // 1010 1100 <-> UE
    D_DW    ((byte) 0x9C); // 1001 1100 <-> U_UE




    private short id;

    ComplexDirection(short id) {
        this.id = id;
    }

    public short getID() {
        return this.id;
    }

    public static boolean isNEWS(ComplexDirection direction) { // @debug, change this
        if(direction == null){
            return false;
        }
        short id = direction.getID();
        if((id&0x80) == 0){ // id is null
            return false;
        }

        return (id & 0x73) == 0;
    }
}
