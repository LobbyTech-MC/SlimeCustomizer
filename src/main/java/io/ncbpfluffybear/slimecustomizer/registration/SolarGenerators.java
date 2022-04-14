package io.ncbpfluffybear.slimecustomizer.registration;

import dev.j3fftw.extrautils.utils.LoreBuilderDynamic;
import io.ncbpfluffybear.slimecustomizer.SlimeCustomizer;
import io.ncbpfluffybear.slimecustomizer.Utils;
import io.ncbpfluffybear.slimecustomizer.objects.CustomSolarGenerator;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link SolarGenerators} registers the generators
 * in the generators config file.
 *
 * @author NCBPFluffyBear
 */
public class SolarGenerators {

    public static boolean register(Config generators) {
        for (String generatorKey : generators.getKeys()) {
            if (generatorKey.equals("EXAMPLE_SOLAR_GENERATOR")) {
                SlimeCustomizer.getInstance().getLogger().log(Level.WARNING, "Your solar-generators.yml file still contains" +
                    " the example solar generator! " +
                    "Did you forget to set up the plugin?");
            }

            int dayEnergy;
            int nightEnergy;
            ItemStack item;
            SlimefunItemStack tempStack;

            ItemGroup category = Utils.getCategory(generators.getString(generatorKey + ".category"), generatorKey);
            if (category == null) {return false;}

            // Day time energy rate
            try {
                dayEnergy = Integer.parseInt(generators.getString(generatorKey + ".stats.energy-production.day"));
            } catch (NumberFormatException e) {
                Utils.disable("The daytime energy production for " + generatorKey + " must be a positive integer!");
                return false;
            }

            // Night time energy rate
            try {
                nightEnergy = Integer.parseInt(generators.getString(generatorKey + ".stats.energy-production.night"));
            } catch (NumberFormatException e) {
                Utils.disable("The nighttime energy production for " + generatorKey + " must be a positive integer!");
                return false;
            }

            // Item material
            item = Utils.getBlockFromConfig(generatorKey, generators.getString(generatorKey + ".block-type"));
            if (item == null) {return false;}

            // Crafting recipe type
            String recipeTypeString = generators.getString(generatorKey + ".crafting-recipe-type").toUpperCase();
            RecipeType recipeType = Utils.getRecipeType(recipeTypeString, generatorKey);
            if (recipeType == null) {
                Utils.disable("The crafting-recipe-type for " + generatorKey + " is not valid! Refer to the wiki to see" +
                    " acceptable inputs.");
                return false;
            }

            // Crafting recipe
            ItemStack[] recipe = Utils.buildCraftingRecipe(generators, generatorKey, recipeType);
            if (recipe == null) {return false;}

            // Building lore
            List<String> itemLore = Utils.colorList(Stream.concat(
                generators.getStringList(generatorKey + "." + "generator-lore").stream(),
                new ArrayList<>(Arrays.asList("", "&e太阳能发电机",
                    LoreBuilderDynamic.powerPerTick(dayEnergy) + " &7(昼)",
                    LoreBuilderDynamic.powerPerTick(nightEnergy) + " &7(夜)"
                )).stream()
            ).collect(Collectors.toList()));

            tempStack = new SlimefunItemStack(generatorKey, item, generators.getString(generatorKey + ".generator-name"));

            // Adding lore
            ItemMeta tempMeta = tempStack.getItemMeta();
            tempMeta.setLore(itemLore);
            tempStack.setItemMeta(tempMeta);

            new CustomSolarGenerator(category, dayEnergy, nightEnergy, tempStack, recipeType, recipe
            ).register(SlimeCustomizer.getInstance());

            Utils.notify("Generator " + generatorKey + " has been registered!");
        }

        return true;
    }
}
