package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InteractTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;
    private final String description;

    private InteractTrigger(Predicate<SkillContext> matcher, String description) {
        this.matcher = matcher;
        this.description = description;
    }

    public static InteractTrigger of(Material material) {
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null && ctx.usedItem().getType() == material,
            "Clique com: " + formatMaterial(material));
    }

    public static InteractTrigger of(Material... materials) {
        Set<Material> set = Arrays.stream(materials).collect(Collectors.toSet());
        String desc = Arrays.stream(materials)
            .map(InteractTrigger::formatMaterial)
            .collect(Collectors.joining(" ou "));
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null && set.contains(ctx.usedItem().getType()),
            "Clique com: " + desc);
    }

    public static InteractTrigger of(Tag<Material> tag) {
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null && tag.isTagged(ctx.usedItem().getType()),
            "Clique em: " + formatTag(tag));
    }

    public static InteractTrigger of(Material material, String heldToolSuffix) {
        String suffixName = switch (heldToolSuffix) {
            case "AXE" -> "um Machado";
            case "PICKAXE" -> "uma Picareta";
            default -> heldToolSuffix;
        };
        return new InteractTrigger(ctx -> {
            if (ctx.usedItem() == null || ctx.usedItem().getType() != material) return false;
            ItemStack mainHand = ctx.player().getInventory().getItemInMainHand();
            return mainHand.getType().name().endsWith("_" + heldToolSuffix);
        }, "Clique com: " + formatMaterial(material) + " (segurando " + suffixName + ")");
    }

    public static InteractTrigger withAmount(Material material, int minAmount) {
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null &&
            ctx.usedItem().getType() == material &&
            ctx.usedItem().getAmount() >= minAmount,
            "Clique com: " + formatMaterial(material) + " (" + minAmount + "+)");
    }

    public static InteractTrigger custom(Predicate<SkillContext> predicate) {
        return new InteractTrigger(predicate, "Clique em um item especifico");
    }

    public static InteractTrigger custom(Predicate<SkillContext> predicate, String description) {
        return new InteractTrigger(predicate, description);
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.INTERACT);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }

    @Override
    public List<String> activationDescription() {
        return List.of(description);
    }

    private static String formatMaterial(Material m) {
        return switch (m) {
            case NETHER_WART -> "<#FF4444>Fungo do Nether";
            case SUGAR -> "<white>Acucar";
            case SLIME_BALL -> "<green>Bola de Slime";
            case LAPIS_LAZULI -> "<blue>Lapis Lazuli";
            case MAGMA_CREAM -> "<gold>Creme de Magma";
            case DRAGON_BREATH -> "<light_purple>Bafo do Dragão";
            case AMETHYST_SHARD -> "<light_purple>Fragmento de Ametista";
            case GUNPOWDER -> "<gray>Polvora";
            case ENDER_PEARL -> "<dark_green>Pepita de Ender";
            case COAL -> "<black>Carvao</black>";
            case CHARCOAL -> "<dark_gray>Carvao Vegetal</dark_gray>";
            case TORCH -> "<gold>Tocha";
            case GLOWSTONE_DUST -> "<yellow>Po de Pedra Luminosa";
            case GOLD_INGOT -> "<gold>Barra de Ouro";
            case IRON_INGOT -> "<white>Barra de Ferro";
            case FLINT -> "<dark_gray>Pedra";
            case OBSIDIAN -> "<dark_purple>Obsidiana";
            case REDSTONE_BLOCK -> "<red>Bloco de Redstone";
            case OAK_LEAVES -> "<green>Folhas de Carvalho";
            case WHEAT -> "<gold>Trigo";
            case BONE_MEAL -> "<white>Farinha de Osso";
            case DIRT -> "<gold>Terra";
            case STONE_BRICKS -> "<gray>Tijolos de Pedra";
            case IRON_BLOCK -> "<white>Bloco de Ferro";
            case CLAY_BALL -> "<gray>Argila";
            case SLIME_BLOCK -> "<green>Bloco de Slime";
            case FEATHER -> "<white>Pena";
            case SHEARS -> "<gray>Tesoura";
            default -> "<white>" + formatName(m);
        };
    }

    private static String formatTag(Tag<Material> tag) {
        if (tag == Tag.FLOWERS) return "<red>Flores</red>";
        if (tag == Tag.LEAVES) return "<green>Folhas</green>";
        return "<white>" + tag.getKey().getKey();
    }

    private static String formatName(Material m) {
        String[] parts = m.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
