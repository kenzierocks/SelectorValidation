
package me.kenzierocks.plugins.selectorvalidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.selector.Selector;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;

@Plugin(id = SelectorValidation.ID, name = SelectorValidation.NAME, version = SelectorValidation.VERSION)
public class SelectorValidation {

    // TODO make an AP to replace at runtime
    // public static final String ID = "@ID@";
    // public static final String NAME = "@NAME@";
    // public static final String VERSION = "@VERSION@";
    public static final String ID = "selectorvalidation";
    public static final String NAME = "SelectorValidation";
    public static final String VERSION = "0.1.0-SNAPSHOT";
    private static SelectorValidation INSTANCE;

    public static SelectorValidation getInstance() {
        return INSTANCE;
    }

    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    {
        INSTANCE = this;
    }

    private SpongeExecutorService executor;

    public Logger getLogger() {
        return this.logger;
    }

    public SpongeExecutorService getExecutor() {
        if (this.executor == null) {
            this.executor = Sponge.getScheduler().createSyncExecutor(this);
        }
        return this.executor;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.logger.info("Loading " + NAME + " v" + VERSION);
        try {
            Files.createDirectories(this.configDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot use the plugin with no configs!", e);
        }
        Sponge.getCommandManager().register(this,
                CommandSpec.builder().arguments(GenericArguments.string(Text.of("selector")))
                        .description(Text.of("Tests a selector using both MC and Sponge methods."))
                        .executor((src, args) -> {
                            String selector = args.<String> getOne("selector").orElse("");
                            if (selector.isEmpty()) {
                                src.sendMessage(Text.of(TextColors.RED, "Please use a selector."));
                                return CommandResult.empty();
                            }
                            src.sendMessage(Text.of("Selecting MC..."));
                            @SuppressWarnings("unchecked")
                            List<Entity> l = (List<Entity>) (Object) EntitySelector.matchEntities((ICommandSender) src,
                                    selector, net.minecraft.entity.Entity.class);
                            sendEntityList(src, l);
                            src.sendMessage(Text.of("Selecting Sponge..."));
                            l = Selector.parse(selector).resolve(src);
                            sendEntityList(src, l);
                            return CommandResult.success();
                        }).build(),
                "select");
        Sponge.getCommandManager().register(this,
                CommandSpec.builder().arguments(GenericArguments.entity(Text.of("selector")))
                        .description(Text.of("Tests a selector using Sponge methods.")).executor((src, args) -> {
                            Collection<Entity> selector = args.<Entity> getAll("selector");
                            sendEntityList(src, ImmutableList.copyOf(selector));
                            return CommandResult.success();
                        }).build(),
                "printone");
        this.logger.info("Loaded " + NAME + " v" + VERSION);
    }

    private void sendEntityList(CommandSource src, List<Entity> l) {
        for (Entity e : l) {
            src.sendMessage(e.get(Keys.DISPLAY_NAME).orElse(Text.of(e.getType().getName())));
        }
        src.sendMessage(Text.of("Entity count: " + l.size()));
    }

    public Path getConfigDir() {
        return this.configDir;
    }

    public Path getAccountSerializationDir() {
        return this.configDir.resolve("accounts");
    }

}
