package com.iwaliner.urushi.blockentity;

import com.iwaliner.urushi.BlockEntityRegister;
import com.iwaliner.urushi.block.MirrorBlock;
import com.iwaliner.urushi.util.ComplexDirection;
import com.iwaliner.urushi.util.ElementType;
import com.iwaliner.urushi.util.ElementUtils;
import com.iwaliner.urushi.util.interfaces.Mirror;
import com.iwaliner.urushi.util.interfaces.ReiryokuImportable;
import com.iwaliner.urushi.util.interfaces.ReiryokuStorable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

public class MirrorBlockEntity extends AbstractReiryokuStorableBlockEntity  implements Mirror {
    public boolean canReach;
    protected int incidentDirection;
    public MirrorBlockEntity(BlockPos p_155550_, BlockState p_155551_) {
        super(BlockEntityRegister.Mirror.get(),100, p_155550_, p_155551_);
    }
    public void load(CompoundTag tag) {
        super.load(tag);
        this.canReach = tag.getBoolean("canReach");
        this.incidentDirection = tag.getInt("incidentDirection");

    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("canReach", this.canReach);
        tag.putInt("incidentDirection", this.incidentDirection);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("canReach", this.canReach);
        compoundtag.putInt("incidentDirection", this.incidentDirection);
        this.putBaseTag(compoundtag);
        return compoundtag;
    }
    private void setCanReach(boolean b){
        this.canReach=b;
    }
@Override
    public boolean getCanReach(){
        return this.canReach;
    }

    /**入射角を参照*/
    @Override
    public ComplexDirection getIncidentDirection() {
        ComplexDirection d=null;
        for ( ComplexDirection f :ComplexDirection.values()) {
            if (f.getID() == incidentDirection) {
                d=f;
                break;
            }
        }
        return d;
    }

    // @debug
    // luoxueyasha 2025/10/2:
    // THIS PIECE OF CODE IS SH*T. Unfortunately I cannot figure out what the f*ck these if-else-es are doing,
    // as I may corrupt these code. AI failed this challenge. If you are reading this and you are going to optimize
    // these, good luck.

    // @debug, todo: pull these functions out of this BE section. luoxueyasha 2025/10/10

    /**入射角を設定*/
    @Override
    public void setIncidentDirection(ComplexDirection direction) {
        this.incidentDirection=direction.getID();
    }
    /**入射角を設定*/
    @Override
    public void setIncidentDirection(Direction direction) {
        if(direction==Direction.NORTH){
            this.incidentDirection= ComplexDirection.N.getID();
        }else if(direction==Direction.SOUTH){
            this.incidentDirection= ComplexDirection.S.getID();
        } else if(direction==Direction.EAST){
            this.incidentDirection= ComplexDirection.E.getID();
        }else if(direction==Direction.WEST){
            this.incidentDirection= ComplexDirection.W.getID();
        }else if(direction==Direction.UP){
            this.incidentDirection= ComplexDirection.U_NSd.getID();
        }else if(direction==Direction.DOWN){
            this.incidentDirection= ComplexDirection.D_NSd.getID();
        }
    }



    public static ComplexDirection getDirectionFromID(short i){
        if((i&0x80) == 0){ // i is null
            return ComplexDirection.FAIL;
        }

        if((i & 0x70) == 0){ // has no u/d info
            i &= 0x0F;
            return switch (i){
                case 0 -> ComplexDirection.N;
                case 1 -> ComplexDirection.N_NE;
                case 2 -> ComplexDirection.NE;
                case 3 -> ComplexDirection.E_NE;
                case 4 -> ComplexDirection.E;
                case 5 -> ComplexDirection.E_SE;
                case 6 -> ComplexDirection.SE;
                case 7 -> ComplexDirection.S_SE;
                case 8 -> ComplexDirection.S;
                case 9 -> ComplexDirection.S_SW;
                case 10 -> ComplexDirection.SW;
                case 11 -> ComplexDirection.W_SW;
                case 12 -> ComplexDirection.W;
                case 13 -> ComplexDirection.W_NW;
                case 14 -> ComplexDirection.NW;
                case 15 -> ComplexDirection.N_NW;
                default -> ComplexDirection.FAIL;
            };
        }
        // has U/D info
        i &= 0x7C;
        // @debug, 写不出来回头再想

        return ComplexDirection.FAIL;
    }

    /**180度の方角を返す*/
    public static ComplexDirection getOppositeDirection(ComplexDirection direction){
        short id = direction.getID();
        if((id&0x80) == 0){
            return ComplexDirection.FAIL;
        }
        return getDirectionFromID((byte) (id|0x08));
    }

    /**右回りに90度の方角を返す*/
    private ComplexDirection getClockwise90DegreesDirection(ComplexDirection direction){
        short id = direction.getID();
        if((id&0x80) == 0){ // null
            return ComplexDirection.FAIL;
        }
        if((id&0x70) != 0){ // has U/D info
            return ComplexDirection.FAIL;
        }

        id += 4;
        id &= (byte) 0x8F; // mod16
        return getDirectionFromID(id);
    }

    /**左回りに90度の方角を返す*/
    private ComplexDirection getCounterClockwise90DegreesDirection(ComplexDirection direction){
        short id = direction.getID();
        if((id&0x80) == 0){ // null
            return ComplexDirection.FAIL;
        }
        if((id&0x70) != 0){ // has U/D info
            return ComplexDirection.FAIL;
        }
        id += 12; // -=4
        id &= (byte) 0x8F;

        return getDirectionFromID(id);
    }

    /**右回りに22.5度の方角を返す*/
    private ComplexDirection getClockwiseNeighborDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<16){
            return getDirectionFromID(direction.getID()+1);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-15);
        }
        return ComplexDirection.FAIL;
    }

    /**右回りに45度の方角を返す*/
    private ComplexDirection getClockwise45DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<15){
            return getDirectionFromID(direction.getID()+2);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-14);
        }
        return ComplexDirection.FAIL;
    }
    /**右回りに67.5度の方角を返す*/
    private ComplexDirection getClockwiseDistantDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<14){
            return getDirectionFromID(direction.getID()+3);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-13);
        }
        return ComplexDirection.FAIL;
    }
    /**右回りに135度の方角を返す*/
    private ComplexDirection getClockwise135DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<11){
            return getDirectionFromID(direction.getID()+6);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-10);
        }
        return ComplexDirection.FAIL;
    }

    /**左回りに22.5度の方角を返す*/
    private ComplexDirection getCounterClockwiseNeighborDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<2){
            return getDirectionFromID(direction.getID()+15);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-1);
        }
        return ComplexDirection.FAIL;
    }

    /**左回りに45度の方角を返す*/
    private ComplexDirection getCounterClockwise45DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<3){
            return getDirectionFromID(direction.getID()+14);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-2);
        }
        return ComplexDirection.FAIL;
    }

    /**左回りに67.5度の方角を返す*/
    private ComplexDirection getCounterClockwiseDistantDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<4){
            return getDirectionFromID(direction.getID()+13);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-3);
        }
        return ComplexDirection.FAIL;
    }

    /**左回りに135度の方角を返す*/
    private ComplexDirection getCounterClockwise135DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction.getID()<7){
            return getDirectionFromID(direction.getID()+10);
        }else if(direction.getID()<17){
            return getDirectionFromID(direction.getID()-6);
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの右回りに90度の方角を返す*/
    private ComplexDirection getVerticalClockwise90DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.U_UE;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの左回りに90度の方角を返す*/
    private ComplexDirection getVerticalCounterClockwise90DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.D_DW;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの右回りに45度の方角を返す*/
    private ComplexDirection getVerticalClockwise45DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.E_UE;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの左回りに45度の方角を返す*/
    private ComplexDirection getVerticalCounterClockwise45DegreesDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.D_DE;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの右回りに22.5度の方角を返す*/
    private ComplexDirection getVerticalClockwiseNeighborDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
                return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.E;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの左回りに22.5度の方角を返す*/
    private ComplexDirection getVerticalCounterClockwiseNeighborDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.DE;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの右回りに67.5度の方角を返す*/
    private ComplexDirection getVerticalClockwiseDistantDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.D_WEd;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.UE;
        }
        return ComplexDirection.FAIL;
    }
    /**手前を西かつ左を北にしたとき、または手前を北かつ左を東にしたときの左回りに67.5度の方角を返す*/
    private ComplexDirection getVerticalCounterClockwiseDistantDirection(ComplexDirection direction){
        if(direction==ComplexDirection.FAIL){
            return ComplexDirection.FAIL;
        }else if(direction==ComplexDirection.N){
            return ComplexDirection.D_DN;
        }else if(direction==ComplexDirection.U_NSd){
            return ComplexDirection.N_UN;
        }else if(direction==ComplexDirection.S){
            return ComplexDirection.U_US;
        }else if(direction==ComplexDirection.D_NSd){
            return ComplexDirection.D_DS;
        }else if(direction==ComplexDirection.E){
            return ComplexDirection.D_DE;
        }else if(direction==ComplexDirection.U_WEd){
            return ComplexDirection.E_UE;
        }else if(direction==ComplexDirection.W){
            return ComplexDirection.U_UW;
        }else if(direction==ComplexDirection.D_WEd){
            return ComplexDirection.W_DW;
        }else if(direction==ComplexDirection.N_UN){
            return ComplexDirection.DN;
        }else if(direction==ComplexDirection.UN){
            return ComplexDirection.N_DN;
        }else if(direction==ComplexDirection.U_UN){
            return ComplexDirection.N;
        }else if(direction==ComplexDirection.U_US){
            return ComplexDirection.UN;
        }else if(direction==ComplexDirection.US){
            return ComplexDirection.U_UN;
        }else if(direction==ComplexDirection.S_US){
            return ComplexDirection.U_NSd;
        }else if(direction==ComplexDirection.D_DS){
            return ComplexDirection.US;
        }else if(direction==ComplexDirection.DS){
            return ComplexDirection.S_US;
        }else if(direction==ComplexDirection.S_DS){
            return ComplexDirection.S;
        }else if(direction==ComplexDirection.D_DN){
            return ComplexDirection.DS;
        }else if(direction==ComplexDirection.DN){
            return ComplexDirection.S_DS;
        }else if(direction==ComplexDirection.N_DN){
            return ComplexDirection.D_NSd;
        }else if(direction==ComplexDirection.E_UE){
            return ComplexDirection.DE;
        }else if(direction==ComplexDirection.UE){
            return ComplexDirection.E_DE;
        }else if(direction==ComplexDirection.U_UE){
            return ComplexDirection.E;
        }else if(direction==ComplexDirection.U_UW){
            return ComplexDirection.UE;
        }else if(direction==ComplexDirection.UW){
            return ComplexDirection.U_UE;
        }else if(direction==ComplexDirection.W_UW){
            return ComplexDirection.U_WEd;
        }else if(direction==ComplexDirection.W_DW){
            return ComplexDirection.UW;
        }else if(direction==ComplexDirection.DW){
            return ComplexDirection.W_UW;
        }else if(direction==ComplexDirection.D_DW){
            return ComplexDirection.W;
        }else if(direction==ComplexDirection.D_DE){
            return ComplexDirection.DW;
        }else if(direction==ComplexDirection.DE){
            return ComplexDirection.D_DW;
        }else if(direction==ComplexDirection.E_DE){
            return ComplexDirection.D_WEd;
        }
        return ComplexDirection.FAIL;
    }
    /**特定の角度(パラメータ)が2つの角度の間に入っているかどうか。指定した2つの角度のいずれかとパラメータが一致する場合は含まない。そしてこの処理は45度の範囲内までしか機能していない。*/
    private boolean isIn(ComplexDirection counterclockwiseDirection,ComplexDirection clockwiseDirection,ComplexDirection parameter){
        if(counterclockwiseDirection==clockwiseDirection){
            return false;
        }else if(getClockwiseNeighborDirection(counterclockwiseDirection)==clockwiseDirection){
            return false;
        }else if(getClockwise45DegreesDirection(counterclockwiseDirection)==clockwiseDirection){
            return getClockwiseNeighborDirection(counterclockwiseDirection)==parameter;
        }else if(getClockwiseDistantDirection(counterclockwiseDirection)==clockwiseDirection){
            return getClockwiseNeighborDirection(counterclockwiseDirection)==parameter
                    ||getClockwise45DegreesDirection(counterclockwiseDirection)==parameter;
        }else if(getClockwise90DegreesDirection(counterclockwiseDirection)==clockwiseDirection){
            return getClockwiseNeighborDirection(counterclockwiseDirection)==parameter
                    ||getClockwise45DegreesDirection(counterclockwiseDirection)==parameter
                    ||getClockwiseDistantDirection(counterclockwiseDirection)==parameter;
        }
        return false;
    }

    /**反射させた後の方角を返す*/
    public ComplexDirection reflectedDirection(ComplexDirection block,ComplexDirection incident){
        ComplexDirection reflect=ComplexDirection.FAIL;
        ComplexDirection blockOpposite=getOppositeDirection(block);
        if(incident==ComplexDirection.FAIL||incident==null){
            return null;
        }else if(getClockwise90DegreesDirection(block)==incident|| getClockwise90DegreesDirection(blockOpposite)==incident){
            reflect=ComplexDirection.FAIL; //鏡で進行ストップ
        }else if(block==incident||blockOpposite==incident){
            reflect=incident; //鏡に対して直角に入射したので、もと来た方角へ返す
        }
        else if(getCounterClockwiseDistantDirection(block)==incident){
            reflect=getClockwiseDistantDirection(block); //鏡面から22.5度に入射
        }else if(getClockwiseDistantDirection(block)==incident){
            reflect=getCounterClockwiseDistantDirection(block); //鏡面から22.5度に入射
        }else if(getCounterClockwiseDistantDirection(blockOpposite)==incident){
            reflect=getClockwiseDistantDirection(blockOpposite); //鏡面から22.5度に入射
        }else if(getClockwiseDistantDirection(blockOpposite)==incident){
            reflect=getCounterClockwiseDistantDirection(blockOpposite); //鏡面から22.5度に入射
        }
        else if(getCounterClockwise45DegreesDirection(block)==incident){
            reflect= getClockwise90DegreesDirection(incident); //鏡面から45度に入射
        }else if(getClockwise45DegreesDirection(block)==incident){
            reflect= getCounterClockwise90DegreesDirection(incident); //鏡面から45度に入射
        }else if(getCounterClockwise45DegreesDirection(blockOpposite)==incident){
            reflect= getClockwise90DegreesDirection(incident); //鏡面から45度に入射
        }else if(getClockwise45DegreesDirection(blockOpposite)==incident){
            reflect= getCounterClockwise90DegreesDirection(incident); //鏡面から45度に入射
        }
        else if(getCounterClockwiseNeighborDirection(block)==incident){
            reflect= getClockwise45DegreesDirection(incident); //鏡面から67.5度に入射
        }else if(getClockwiseNeighborDirection(block)==incident){
            reflect= getCounterClockwise45DegreesDirection(incident); //鏡面から67.5度に入射
        }else if(getCounterClockwiseNeighborDirection(blockOpposite)==incident){
            reflect= getClockwise45DegreesDirection(incident); //鏡面から67.5度に入射
        }else if(getClockwiseNeighborDirection(blockOpposite)==incident){
            reflect= getCounterClockwise45DegreesDirection(incident); //鏡面から67.5度に入射
        }
        else if(getVerticalCounterClockwiseDistantDirection(block)==incident){
            reflect=getVerticalClockwiseDistantDirection(block); //鏡面から縦に22.5度に入射
        }else if(getVerticalClockwiseDistantDirection(block)==incident){
            reflect=getVerticalCounterClockwiseDistantDirection(block); //鏡面から縦に22.5度に入射
        }else if(getVerticalCounterClockwiseDistantDirection(blockOpposite)==incident){
            reflect=getVerticalClockwiseDistantDirection(blockOpposite); //鏡面から縦に22.5度に入射
        }else if(getVerticalClockwiseDistantDirection(blockOpposite)==incident){
            reflect=getVerticalCounterClockwiseDistantDirection(blockOpposite); //鏡面から縦に22.5度に入射
        }
        else if(getVerticalCounterClockwise45DegreesDirection(block)==incident){
            reflect= getVerticalClockwise90DegreesDirection(incident); //鏡面から縦に45度に入射
        }else if(getVerticalClockwise45DegreesDirection(block)==incident){
            reflect= getVerticalCounterClockwise90DegreesDirection(incident); //鏡面から縦に45度に入射
        }else if(getVerticalCounterClockwise45DegreesDirection(blockOpposite)==incident){
            reflect= getVerticalClockwise90DegreesDirection(incident); //鏡面から縦に45度に入射
        }else if(getVerticalClockwise45DegreesDirection(blockOpposite)==incident){
            reflect= getVerticalCounterClockwise90DegreesDirection(incident); //鏡面から縦に45度に入射
        }


        else if(getVerticalCounterClockwiseNeighborDirection(block)==incident){
            reflect= getVerticalClockwise45DegreesDirection(incident); //鏡面から縦に67.5度に入射
        }else if(getVerticalClockwiseNeighborDirection(block)==incident){
            reflect= getVerticalCounterClockwise45DegreesDirection(incident); //鏡面から縦に67.5度に入射
        }else if(getVerticalCounterClockwiseNeighborDirection(blockOpposite)==incident){
            reflect= getVerticalClockwise45DegreesDirection(incident); //鏡面から縦に67.5度に入射
        }else if(getVerticalClockwiseNeighborDirection(blockOpposite)==incident){
            reflect= getVerticalCounterClockwise45DegreesDirection(incident); //鏡面から縦に67.5度に入射
        }



        return reflect;
    }
    private int getTier(){
        Block b = getBlockState().getBlock();
        if(b instanceof MirrorBlock){
            return ((MirrorBlock) b).getTier();
        }
        return 0;
    }
    private double getParticleSpeed(){
        return switch (this.getTier()) {
            case 2 -> 0.3D;
            default -> 0.2D;
        };

    }

    private double[] getParticleVelocity(ComplexDirection reflectedDirection){
        double speed=getParticleSpeed();
        double vectorComponent45Degrees=speed/(Math.sqrt(2));
        if(reflectedDirection==ComplexDirection.N){
            return new double[]{0D, 0D, -speed};
        }else if(reflectedDirection==ComplexDirection.E){
            return new double[]{speed, 0D, 0D};
        }else if(reflectedDirection==ComplexDirection.S){
            return new double[]{0D, 0D, speed};
        }else if(reflectedDirection==ComplexDirection.W){
            return new double[]{-speed, 0D, 0D};
        }else if(reflectedDirection==ComplexDirection.NE){
            return new double[]{vectorComponent45Degrees, 0D, -vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.SE){
            return new double[]{vectorComponent45Degrees, 0D, vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.SW){
            return new double[]{-vectorComponent45Degrees, 0D, vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.NW){
            return new double[]{-vectorComponent45Degrees, 0D,-vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.UN){
            return new double[]{0D, vectorComponent45Degrees,-vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.UE){
            return new double[]{vectorComponent45Degrees, vectorComponent45Degrees,0D};
        }else if(reflectedDirection==ComplexDirection.US){
            return new double[]{0D, vectorComponent45Degrees,vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.UW){
            return new double[]{-vectorComponent45Degrees, vectorComponent45Degrees,0D};
        }else if(reflectedDirection==ComplexDirection.DN){
            return new double[]{0D, -vectorComponent45Degrees,-vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.DE){
            return new double[]{vectorComponent45Degrees, -vectorComponent45Degrees,0D};
        }else if(reflectedDirection==ComplexDirection.DS){
            return new double[]{0D, -vectorComponent45Degrees,vectorComponent45Degrees};
        }else if(reflectedDirection==ComplexDirection.DW){
            return new double[]{-vectorComponent45Degrees, -vectorComponent45Degrees,0D};
        }else if(reflectedDirection==ComplexDirection.U_NSd ||reflectedDirection==ComplexDirection.U_WEd){
            return new double[]{0D, speed, 0D};
        }else if(reflectedDirection==ComplexDirection.D_NSd ||reflectedDirection==ComplexDirection.D_WEd){
            return new double[]{0D, -speed, 0D};
        }
        return new double[]{0D, 0D, 0D};
    }
    private double[] getParticlePos(ComplexDirection reflectedDirection,BlockPos pos){
        double i=0.6D;
        double j=i/(Math.sqrt(2));
        if(reflectedDirection==ComplexDirection.N){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D, pos.getZ()+0.5D-i};
        }else if(reflectedDirection==ComplexDirection.E){
            return new double[]{pos.getX()+0.5D+i,pos.getY()+0.5D, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.S){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D, pos.getZ()+0.5D+i};
        }else if(reflectedDirection==ComplexDirection.W){
            return new double[]{pos.getX()+0.5D-i,pos.getY()+0.5D, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.NE){
            return new double[]{pos.getX()+0.5D+j,pos.getY()+0.5D, pos.getZ()+0.5D-j};
        }else if(reflectedDirection==ComplexDirection.SE){
            return new double[]{pos.getX()+0.5D+j,pos.getY()+0.5D, pos.getZ()+0.5D+j};
        }else if(reflectedDirection==ComplexDirection.SW){
            return new double[]{pos.getX()+0.5D-j,pos.getY()+0.5D, pos.getZ()+0.5D+j};
        }else if(reflectedDirection==ComplexDirection.NW){
            return new double[]{pos.getX()+0.5D-j,pos.getY()+0.5D, pos.getZ()+0.5D-j};
        }else if(reflectedDirection==ComplexDirection.UN){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D+j, pos.getZ()+0.5D-j};
        }else if(reflectedDirection==ComplexDirection.UE){
            return new double[]{pos.getX()+0.5D+j,pos.getY()+0.5D+j, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.US){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D+j, pos.getZ()+0.5D+j};
        }else if(reflectedDirection==ComplexDirection.UW){
            return new double[]{pos.getX()+0.5D-j,pos.getY()+0.5D+j, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.DN){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D-j, pos.getZ()+0.5D-j};
        }else if(reflectedDirection==ComplexDirection.DE){
            return new double[]{pos.getX()+0.5D+j,pos.getY()+0.5D-j, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.DS){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D-j, pos.getZ()+0.5D+j};
        }else if(reflectedDirection==ComplexDirection.DW){
            return new double[]{pos.getX()+0.5D-j,pos.getY()+0.5D-j, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.U_NSd){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D+i, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.U_WEd){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D+i, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.D_NSd){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D-i, pos.getZ()+0.5D};
        }else if(reflectedDirection==ComplexDirection.D_WEd){
            return new double[]{pos.getX()+0.5D,pos.getY()+0.5D-i, pos.getZ()+0.5D};
        }
        return new double[]{pos.getX()+0.5D,pos.getY()+0.5D, pos.getZ()+0.5D};
    }

    public static Direction[] getDirectionFromComplexDirection(ComplexDirection direction){
        if(direction==ComplexDirection.N){
            return new Direction[]{Direction.NORTH,null};
        }else if(direction==ComplexDirection.E){
            return new Direction[]{Direction.EAST,null};
        }else if(direction==ComplexDirection.S){
            return new Direction[]{Direction.SOUTH,null};
        }else if(direction==ComplexDirection.W){
            return new Direction[]{Direction.WEST,null};
        }else if(direction==ComplexDirection.NE){
            return new Direction[]{Direction.NORTH,Direction.EAST};
        }else if(direction==ComplexDirection.SE){
            return new Direction[]{Direction.SOUTH,Direction.EAST};
        }else if(direction==ComplexDirection.SW){
            return new Direction[]{Direction.SOUTH,Direction.WEST};
        }else if(direction==ComplexDirection.NW){
            return new Direction[]{Direction.NORTH,Direction.WEST};
        }else if(direction==ComplexDirection.UN){
            return new Direction[]{Direction.UP,Direction.NORTH};
        }else if(direction==ComplexDirection.UE){
            return new Direction[]{Direction.UP,Direction.EAST};
        }else if(direction==ComplexDirection.US){
            return new Direction[]{Direction.UP,Direction.SOUTH};
        }else if(direction==ComplexDirection.UW){
            return new Direction[]{Direction.UP,Direction.WEST};
        }else if(direction==ComplexDirection.U_NSd){
            return new Direction[]{Direction.UP,null};
        }else if(direction==ComplexDirection.U_WEd){
            return new Direction[]{Direction.UP,null};
        }else if(direction==ComplexDirection.DN){
            return new Direction[]{Direction.DOWN,Direction.NORTH};
        }else if(direction==ComplexDirection.DE){
            return new Direction[]{Direction.DOWN,Direction.EAST};
        }else if(direction==ComplexDirection.DS){
            return new Direction[]{Direction.DOWN,Direction.SOUTH};
        }else if(direction==ComplexDirection.DW){
            return new Direction[]{Direction.DOWN,Direction.WEST};
        }else if(direction==ComplexDirection.D_NSd){
            return new Direction[]{Direction.DOWN,null};
        }else if(direction==ComplexDirection.D_WEd){
            return new Direction[]{Direction.DOWN,null};
        }else{
            return null;
        }
    }
    /**一度に送信する霊力の量*/
    private int getSendAmount(){
        int i= getTier()==2? 5 : 1;
        int r = getStoredReiryoku();
        if(r-i<0){
            return r;
        }
        return i;

    }

    @Override
    public ElementType getStoredElementType() {
        return this.getReceiveElementType();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MirrorBlockEntity blockEntity) {
      if(state.getBlock()instanceof MirrorBlock) {
          blockEntity.recieveReiryoku(level, pos);

          boolean canReach = blockEntity.getCanReach();
          ComplexDirection mirrorDirection = getDirectionFromID(state.getValue(MirrorBlock.DIRECTION));
          ComplexDirection incidentDirection = blockEntity.getIncidentDirection();
          int storedReiryoku = blockEntity.getStoredReiryoku();
          ComplexDirection reflectedDirection = blockEntity.reflectedDirection(mirrorDirection, incidentDirection);
          BlockPos goalPos = blockEntity.findImportableBlock(level, pos, reflectedDirection);




      /*  List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(2D));
        if (!list.isEmpty()) {
            for (LivingEntity entity : list) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    MutableComponent component = Component.translatable("info.test");
                    String s1=incidentDirection==null? "null":incidentDirection.name();
                    String s2=mirrorDirection==null? "null":mirrorDirection.name();
                    String s3=reflectedDirection==null? "null":reflectedDirection.name();
                    String s4=blockEntity.getOppositeDirection(mirrorDirection)==null? "null":blockEntity.getOppositeDirection(mirrorDirection).name();

                    player.displayClientMessage(component.append(", incident: "+s1).append(", mirror: "+s2).append(", reflect: "+s3).append(", opposide: "+s4).withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.UNDERLINE), true);

                }
            }
        }*/


          if (reflectedDirection == null) {
              blockEntity.setCanReach(true);
          } else if (reflectedDirection == ComplexDirection.FAIL || goalPos == pos) {

              blockEntity.setCanReach(false);
          } else {

              blockEntity.setCanReach(true);
          }
          if (storedReiryoku > 0) {
              if (canReach) {

                  if (goalPos != pos) {

                      blockEntity.send(level, pos, goalPos, reflectedDirection);
                  }
              }


          }
      }
    }
    private BlockPos findImportableBlock(Level level,BlockPos mirrorPos,ComplexDirection incidentDirection){
        boolean b1=incidentDirection==ComplexDirection.N||incidentDirection==ComplexDirection.S||incidentDirection==ComplexDirection.E||incidentDirection==ComplexDirection.W;
        int range= b1? Mth.floor(getParticleSpeed()*80+0.6D) : Mth.floor((getParticleSpeed()*80+0.6D)/Math.sqrt(2));
        MirrorBlockEntity mirrorBlockEntity= (MirrorBlockEntity) level.getBlockEntity(mirrorPos);

            int t1=0;
            if(getDirectionFromComplexDirection(incidentDirection)!=null) {

                for (int i1 = 1; i1 < range; i1++) {
                    BlockPos pos = Objects.requireNonNull(getDirectionFromComplexDirection(incidentDirection))[1] == null ? mirrorPos.relative(Objects.requireNonNull(getDirectionFromComplexDirection(incidentDirection))[0], i1) : mirrorPos.relative(Objects.requireNonNull(getDirectionFromComplexDirection(incidentDirection))[0], i1).relative(Objects.requireNonNull(getDirectionFromComplexDirection(incidentDirection))[1], i1);
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    BlockState state = level.getBlockState(pos);
                    VoxelShape shape = state.getCollisionShape(level, pos).optimize();
                    double corner = 6D;
                    VoxelShape particleShape = Block.box(corner, corner, corner, 16D - corner, 16D - corner, 16D - corner);

                    if (blockEntity instanceof ReiryokuImportable) {
                        ReiryokuStorable reiryokuStorable = (ReiryokuStorable) blockEntity;
                        if (reiryokuStorable.canAddReiryoku(mirrorBlockEntity.getSendAmount())) {
                            if (reiryokuStorable.getStoredElementType() == mirrorBlockEntity.getStoredElementType()) {
                                t1 = i1;
                                break;
                            }
                        }
                        break;
                    } else if (blockEntity instanceof Mirror&&level.getBlockState(pos).getBlock() instanceof MirrorBlock) {
                        ReiryokuStorable reiryokuStorable = (ReiryokuStorable) blockEntity;
                        Mirror mirror = (Mirror) blockEntity;
                        if (reiryokuStorable.canAddReiryoku(mirrorBlockEntity.getSendAmount())) {
                            ComplexDirection blockDirection = getDirectionFromID(level.getBlockState(pos).getValue(MirrorBlock.DIRECTION));
                            if (getClockwise90DegreesDirection(blockDirection) != incidentDirection && getCounterClockwise90DegreesDirection(blockDirection) != incidentDirection) {
                                t1 = i1;
                                break;
                            }else if (getVerticalClockwise90DegreesDirection(blockDirection) != incidentDirection && getVerticalCounterClockwise90DegreesDirection(blockDirection) != incidentDirection) {
                                t1 = i1;
                                break;
                            }
                        }
                        break;
                    } else if (Shapes.joinIsNotEmpty(shape, particleShape, BooleanOp.AND)) {
                        break;
                    }
                }

                return getDirectionFromComplexDirection(incidentDirection)[1] == null ? mirrorPos.relative(getDirectionFromComplexDirection(incidentDirection)[0], t1) : mirrorPos.relative(getDirectionFromComplexDirection(incidentDirection)[0], t1).relative(getDirectionFromComplexDirection(incidentDirection)[1], t1);
            }
            return mirrorPos;
    }
    private void send(Level level,BlockPos mirrorPos, BlockPos goalPos,ComplexDirection incidentDirection){
        MirrorBlockEntity mirrorBlockEntity= (MirrorBlockEntity) level.getBlockEntity(mirrorPos);
        ReiryokuStorable goalBlockEntity= (ReiryokuStorable) level.getBlockEntity(goalPos);
        int distance=Mth.floor( Math.sqrt( (Math.abs(mirrorPos.getX()-goalPos.getX()))^2+(Math.abs(mirrorPos.getY()-goalPos.getY()))^2+(Math.abs(mirrorPos.getZ()-goalPos.getZ()))^2));

        int arriveTick= Mth.floor ((distance-1)/getParticleSpeed())<=0? 1:Mth.floor ((distance-1)/getParticleSpeed());

        if(mirrorBlockEntity!=null&&goalBlockEntity!=null&&goalBlockEntity.isIdle()&&mirrorBlockEntity.canDecreaseReiryoku(mirrorBlockEntity.getSendAmount())&&goalBlockEntity.canAddReiryoku(mirrorBlockEntity.getSendAmount())){
            if(goalBlockEntity instanceof ReiryokuImportable){
                goalBlockEntity.setReceiveWaitingTime(arriveTick);
                goalBlockEntity.setReceiveAmount(mirrorBlockEntity.getSendAmount());
                goalBlockEntity.setReceiveElementType(mirrorBlockEntity.getStoredElementType());

                goalBlockEntity.markUpdated();
                mirrorBlockEntity.decreaseStoredReiryoku(mirrorBlockEntity.getSendAmount());
                mirrorBlockEntity.markUpdated();
                double[] velocity=mirrorBlockEntity.getParticleVelocity(incidentDirection);
                double[] position=mirrorBlockEntity.getParticlePos(incidentDirection,mirrorPos);
                level.addParticle(ElementUtils.getMediumElementParticle(mirrorBlockEntity.getStoredElementType()), position[0], position[1], position[2], velocity[0], velocity[1], velocity[2]);

            }else if(goalBlockEntity instanceof Mirror){
                Mirror mirror= (Mirror) goalBlockEntity;
                if(mirror.getCanReach()) {
                    goalBlockEntity.setReceiveWaitingTime(arriveTick);
                    goalBlockEntity.setReceiveAmount(mirrorBlockEntity.getSendAmount());
                    goalBlockEntity.setReceiveElementType(mirrorBlockEntity.getStoredElementType());
                       mirror.setIncidentDirection(getOppositeDirection(incidentDirection));
                    goalBlockEntity.markUpdated();
                    mirrorBlockEntity.decreaseStoredReiryoku(mirrorBlockEntity.getSendAmount());
                    mirrorBlockEntity.markUpdated();
                    double[] velocity = mirrorBlockEntity.getParticleVelocity(incidentDirection);
                    double[] position = mirrorBlockEntity.getParticlePos(incidentDirection, mirrorPos);
                    level.addParticle(ElementUtils.getMediumElementParticle(mirrorBlockEntity.getStoredElementType()), position[0], position[1], position[2], velocity[0], velocity[1], velocity[2]);
                }
            }
        }
    }
}
