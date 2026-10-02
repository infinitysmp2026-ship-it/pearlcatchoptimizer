package com.example.pearlcatchoptimizer.config;

import com.example.pearlcatchoptimizer.PearlCatchOptimizerClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Simple properties-file config: .minecraft/config/pearlcatchoptimizer.properties */
public final class PearlCatchConfig {
    /** Master switch. */
    public boolean enabled = true;
    /** Enables the left-click Pearl Catch sequence. */
    public boolean pearlCatchEnabled = true;
    /**
     * Extra client ticks to wait between steps. 0 = minimum valid transition
     * (next point at which Minecraft's own state allows the following step).
     */
    public int sequenceDelayTicks = 0;
    /** true: sequence only starts if a Wind Charge is in the hotbar (otherwise left click stays vanilla). */
    public boolean windChargeRequired = true;
    /** Log state transitions. */
    public boolean debug = false;
    /** Safety timeout (ticks) for waiting on Minecraft to accept an action. */
    public int maxWaitTicks = 20;

    public static PearlCatchConfig load() {
        PearlCatchConfig cfg = new PearlCatchConfig();
        Path file = FabricLoader.getInstance().getConfigDir().resolve("pearlcatchoptimizer.properties");
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                props.load(reader);
                cfg.enabled = bool(props, "enabled", cfg.enabled);
                cfg.pearlCatchEnabled = bool(props, "pearlCatchEnabled", cfg.pearlCatchEnabled);
                cfg.sequenceDelayTicks = integer(props, "sequenceDelayTicks", cfg.sequenceDelayTicks);
                cfg.windChargeRequired = bool(props, "windChargeRequired", cfg.windChargeRequired);
                cfg.debug = bool(props, "debug", cfg.debug);
                cfg.maxWaitTicks = integer(props, "maxWaitTicks", cfg.maxWaitTicks);
            } catch (IOException | RuntimeException e) {
                PearlCatchOptimizerClient.LOGGER.warn("Could not read config, using defaults", e);
            }
        }
        cfg.sequenceDelayTicks = Math.max(0, Math.min(20, cfg.sequenceDelayTicks));
        cfg.maxWaitTicks = Math.max(2, Math.min(200, cfg.maxWaitTicks));
        cfg.save(file); // rewrite so new keys always appear
        return cfg;
    }

    private void save(Path file) {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("pearlCatchEnabled", Boolean.toString(pearlCatchEnabled));
        p.setProperty("sequenceDelayTicks", Integer.toString(sequenceDelayTicks));
        p.setProperty("windChargeRequired", Boolean.toString(windChargeRequired));
        p.setProperty("debug", Boolean.toString(debug));
        p.setProperty("maxWaitTicks", Integer.toString(maxWaitTicks));
        try (Writer w = Files.newBufferedWriter(file)) {
            p.store(w, "Pearl Catch Optimizer - sequenceDelayTicks 0 = minimum valid transition");
        } catch (IOException e) {
            PearlCatchOptimizerClient.LOGGER.warn("Could not write config", e);
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int integer(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
