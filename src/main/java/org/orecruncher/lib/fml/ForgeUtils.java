/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib.fml;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class ForgeUtils {
    private ForgeUtils() {

    }

    @Nonnull
    public static Optional<? extends ModContainer> findModContainer(@Nonnull final String modId) {
        return ModList.get().getModContainerById(modId);
    }

    @Nonnull
    public static Optional<IModInfo> getModInfo(@Nonnull final String modId) {
        return findModContainer(modId).map(ModContainer::getModInfo);
    }

    @Nonnull
    public static String getModDisplayName(@Nonnull final String modId) {
        if ("minecraft".equalsIgnoreCase(modId))
            return "Minecraft";
        return getModInfo(modId).map(IModInfo::getDisplayName).orElse("UNKNOWN");
    }

    @Nonnull
    public static String getModDisplayName(@Nonnull final ResourceLocation resource) {
        Objects.requireNonNull(resource);
        return getModDisplayName(resource.getNamespace());
    }

    @Nullable
    public static ArtifactVersion getForgeVersion() {
        return getModInfo("forge").map(IModInfo::getVersion).orElse(null);
    }

    @Nonnull
    public static List<String> getModIdList() {
        return ModList.get().getModFiles()
                .stream()
                .flatMap(e -> e.getMods().stream())
                .map(IModInfo::getModId)
                .distinct()
                .collect(Collectors.toList());
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static List<String> getResourcePackIdList() {
        return GameUtils.getMC().getResourcePackList().getEnabledPacks()
                .stream()
                .flatMap(e -> e.getResourcePack().getResourceNamespaces(ResourcePackType.CLIENT_RESOURCES).stream())
                .collect(Collectors.toList());
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static List<ResourceLocation> getResourceLocations(@Nonnull final String path) {
        return Stream.concat(
                    ForgeUtils.getModIdList().stream(),
                    ForgeUtils.getResourcePackIdList().stream()
                )
                .distinct()
                .map(e -> new ResourceLocation(e, path))
                .collect(Collectors.toList());
    }

    public static Map<ResourceLocation, Class<? extends Entity>> getRegisteredEntities() {
        final Map<ResourceLocation, Class<? extends Entity>> results = new HashMap<>();

        final Collection<EntityType<?>> f = ForgeRegistries.ENTITIES.getValues();
        for (final EntityType<?> et : f) {
            try {

                // Code should work as it relates to Minecraft.  Should have a dummy world.
                final Entity entity = et.create(null);
                results.put(et.getRegistryName(), entity.getClass());

            } catch (@Nonnull final Throwable t) {
                Lib.LOGGER.warn("Unable to instantiate '%s'", et.getRegistryName().toString());
            }
        }

        return results;
    }

}
