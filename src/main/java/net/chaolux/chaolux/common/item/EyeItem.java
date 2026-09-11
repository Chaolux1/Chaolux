package net.chaolux.chaolux.common.item;

import net.chaolux.chaolux.common.event.EyeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EyeItem extends Item {
    public EyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level=useOnContext.getLevel();
        Player player=useOnContext.getPlayer();
        BlockPos blockPos=useOnContext.getClickedPos();
        if(!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity=level.getBlockEntity(blockPos);
            if(blockEntity != null) {
                EyeEvent.copyTag(serverPlayer,"BlockEntity " + blockPos.toShortString(),new BlockDataAccessor(blockEntity,blockPos).getData());
            } else {
                EyeEvent.getNoBlockEntity(serverPlayer,blockPos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack=player.getItemInHand(interactionHand);
        if(!level.isClientSide && player instanceof ServerPlayer serverPlayer) EyeEvent.copyTag(serverPlayer,"Player " + serverPlayer.getScoreboardName(),new EntityDataAccessor(serverPlayer).getData());
        return InteractionResultHolder.sidedSuccess(itemStack,level.isClientSide);
    }
}
