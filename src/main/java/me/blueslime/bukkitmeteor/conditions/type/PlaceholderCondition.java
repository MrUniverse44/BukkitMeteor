package me.blueslime.bukkitmeteor.conditions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.conditions.condition.Condition;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderCondition extends Condition {

    public PlaceholderCondition() {
        super("[placeholder]", "<placeholder>");
    }

    @Override
    public boolean execute(BukkitMeteorPlugin plugin, String parameter, Player player, TextReplacer replacer) {
        String placeholder = replace(parameter);

        if (replacer != null) {
            placeholder = replacer.apply(placeholder);
        }

        if (plugin.isPluginEnabled("PlaceholderAPI")) {
            placeholder = PlaceholderAPI.setPlaceholders(player, placeholder);
        }

        String[] operations = placeholder.split("<orElse>");

        boolean result = executeValues(player, operations[0]);

        if (operations.length > 1 && !result) {
            String[] secondaryOperations = operations[1].split(";;");
            fetch(Actions.class).execute(Arrays.asList(secondaryOperations), player, replacer);
        }

        return result;
    }

    private boolean executeValues(Player player, String parameter) {
        Pattern pattern = Pattern.compile("^(.*?)\\s*(==|>=|<=|>|<|!=|=i=|\\|-|-\\|)\\s*(.*?)$");
        Matcher matcher = pattern.matcher(parameter);
        if (!matcher.find()) {
            getLogs().error("Invalid format. Use 'value1 operator value2': " + parameter);
            return false;
        }
        String value1 = matcher.group(1).trim();
        String operator = matcher.group(2).trim();
        String value2 = matcher.group(3).trim();

        return compareValues(value1, value2, operator);
    }

    private boolean compareValues(String value1, String value2, String operator) {
        try {
            double num1 = Double.parseDouble(value1);
            double num2 = Double.parseDouble(value2);
            return switch (operator) {
                case "==" -> num1 == num2;
                case ">" -> num1 > num2;
                case ">=" -> num1 >= num2;
                case "<" -> num1 < num2;
                case "<=" -> num1 <= num2;
                case "!=" -> num1 != num2;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return switch (operator) {
                case "==" -> value1.equals(value2);
                case ">" -> value1.compareTo(value2) > 0;
                case ">=" -> value1.compareTo(value2) >= 0;
                case "<" -> value1.compareTo(value2) < 0;
                case "<=" -> value1.compareTo(value2) <= 0;
                case "!=" -> !value1.equals(value2);
                case "|-" -> value1.startsWith(value2);
                case "-|" -> value1.endsWith(value2);
                case "=i=" -> value1.equalsIgnoreCase(value2);
                default -> false;
            };
        }
    }
}
