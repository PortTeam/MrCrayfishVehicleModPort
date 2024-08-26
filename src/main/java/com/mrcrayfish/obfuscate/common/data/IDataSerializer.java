package com.mrcrayfish.obfuscate.common.data;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public interface IDataSerializer<T>
{
    void write(FriendlyByteBuf buf, T value);

    T read(FriendlyByteBuf buf);

    Tag write(T value);

    T read(Tag nbt);
}