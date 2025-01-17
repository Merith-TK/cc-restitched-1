/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2021. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.TurtleUpgrades;
import dan200.computercraft.shared.common.IColouredItem;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ItemComputerBase;
import dan200.computercraft.shared.turtle.blocks.BlockTurtle;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import javax.annotation.Nonnull;

import static dan200.computercraft.shared.turtle.core.TurtleBrain.*;

public class ItemTurtle extends ItemComputerBase implements ITurtleItem
{
    public ItemTurtle( BlockTurtle block, Settings settings )
    {
        super( block, settings );
    }

    @Override
    public void appendStacks( @Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> list )
    {
        if( !isIn( group ) )
        {
            return;
        }

        ComputerFamily family = getFamily();

        list.add( create( -1, null, -1, null, null, 0, null ) );
        TurtleUpgrades.getVanillaUpgrades()
            .filter( x -> TurtleUpgrades.suitableForFamily( family, x ) )
            .map( x -> create( -1, null, -1, null, x, 0, null ) )
            .forEach( list::add );
    }

    public ItemStack create( int id, String label, int colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, int fuelLevel, Identifier overlay )
    {
        // Build the stack
        ItemStack stack = new ItemStack( this );
        if( label != null )
        {
            stack.setCustomName( new LiteralText( label ) );
        }
        if( id >= 0 )
        {
            stack.getOrCreateNbt()
                .putInt( NBT_ID, id );
        }
        IColouredItem.setColourBasic( stack, colour );
        if( fuelLevel > 0 )
        {
            stack.getOrCreateNbt()
                .putInt( NBT_FUEL, fuelLevel );
        }
        if( overlay != null )
        {
            stack.getOrCreateNbt()
                .putString( NBT_OVERLAY, overlay.toString() );
        }

        if( leftUpgrade != null )
        {
            stack.getOrCreateNbt()
                .putString( NBT_LEFT_UPGRADE,
                    leftUpgrade.getUpgradeID()
                        .toString() );
        }

        if( rightUpgrade != null )
        {
            stack.getOrCreateNbt()
                .putString( NBT_RIGHT_UPGRADE,
                    rightUpgrade.getUpgradeID()
                        .toString() );
        }

        return stack;
    }

    @Nonnull
    @Override
    public Text getName( @Nonnull ItemStack stack )
    {
        String baseString = getTranslationKey( stack );
        ITurtleUpgrade left = getUpgrade( stack, TurtleSide.LEFT );
        ITurtleUpgrade right = getUpgrade( stack, TurtleSide.RIGHT );
        if( left != null && right != null )
        {
            return new TranslatableText( baseString + ".upgraded_twice",
                new TranslatableText( right.getUnlocalisedAdjective() ),
                new TranslatableText( left.getUnlocalisedAdjective() ) );
        }
        else if( left != null )
        {
            return new TranslatableText( baseString + ".upgraded", new TranslatableText( left.getUnlocalisedAdjective() ) );
        }
        else if( right != null )
        {
            return new TranslatableText( baseString + ".upgraded", new TranslatableText( right.getUnlocalisedAdjective() ) );
        }
        else
        {
            return new TranslatableText( baseString );
        }
    }

    //    @Nullable
    //    @Override
    //    public String getCreatorModId( ItemStack stack )
    //    {
    //        // Determine our "creator mod" from the upgrades. We attempt to find the first non-vanilla/non-CC
    //        // upgrade (starting from the left).
    //
    //        ITurtleUpgrade left = getUpgrade( stack, TurtleSide.LEFT );
    //        if( left != null )
    //        {
    //            String mod = TurtleUpgrades.getOwner( left );
    //            if( mod != null && !mod.equals( ComputerCraft.MOD_ID ) ) return mod;
    //        }
    //
    //        ITurtleUpgrade right = getUpgrade( stack, TurtleSide.RIGHT );
    //        if( right != null )
    //        {
    //            String mod = TurtleUpgrades.getOwner( right );
    //            if( mod != null && !mod.equals( ComputerCraft.MOD_ID ) ) return mod;
    //        }
    //
    //        return super.getCreatorModId( stack );
    //    }

    @Override
    public ITurtleUpgrade getUpgrade( @Nonnull ItemStack stack, @Nonnull TurtleSide side )
    {
        NbtCompound tag = stack.getNbt();
        if( tag == null )
        {
            return null;
        }

        String key = side == TurtleSide.LEFT ? NBT_LEFT_UPGRADE : NBT_RIGHT_UPGRADE;
        return tag.contains( key ) ? TurtleUpgrades.get( tag.getString( key ) ) : null;
    }

    @Override
    public int getFuelLevel( @Nonnull ItemStack stack )
    {
        NbtCompound tag = stack.getNbt();
        return tag != null && tag.contains( NBT_FUEL ) ? tag.getInt( NBT_FUEL ) : 0;
    }

    @Override
    public Identifier getOverlay( @Nonnull ItemStack stack )
    {
        NbtCompound tag = stack.getNbt();
        return tag != null && tag.contains( NBT_OVERLAY ) ? new Identifier( tag.getString( NBT_OVERLAY ) ) : null;
    }

    @Override
    public ItemStack withFamily( @Nonnull ItemStack stack, @Nonnull ComputerFamily family )
    {
        return TurtleItemFactory.create( getComputerID( stack ), getLabel( stack ), getColour( stack ),
            family, getUpgrade( stack, TurtleSide.LEFT ),
            getUpgrade( stack, TurtleSide.RIGHT ), getFuelLevel( stack ), getOverlay( stack ) );
    }
}
