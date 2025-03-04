package it.mikeslab.econexuslite.inventory.template;

import it.mikeslab.commons.api.inventory.CustomInventory;
import it.mikeslab.commons.api.inventory.config.GuiConfig;
import it.mikeslab.commons.api.inventory.config.GuiConfigImpl;
import it.mikeslab.commons.api.inventory.pojo.GuiContext;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.config.ConfigKey;
import lombok.Data;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.util.Optional;

/*
 * This class may get moved to a different package in the future
 */
@Data
@ApiStatus.Internal
public abstract class GuiTemplate implements CustomInventory {

    @Setter
    private GuiContext guiContext;
    private EcoNexusLite instance;

    public GuiTemplate(final EcoNexusLite instance, GuiContext context) {
        this.instance = instance;
        this.guiContext = context;

        this.generate();

        // Setting up for animations
        context
                .getDefaultGuiDetails()
                .setAnimationInterval(
                        instance.getCustomConfig().getInt(ConfigKey.ANIMATION_INTERVAL)
                );
    }

    /**
     * Initialize the gui
     */
    private void generate() {

        File configFile = new File(getInstance().getDataFolder(), this.getRelativePath().toString());
        String fileName = configFile.getName();

        GuiConfig guiConfig;

        // Check if the guiConfig is cached, this
        // allows preventing loading the same config multiple times
        if (this.getInstance().getCachedConfigs().containsKey(fileName)) {

            guiConfig = this.getInstance().getCachedConfigs().get(fileName);

        } else {

            guiConfig = new GuiConfigImpl(this.getInstance());
            guiConfig.loadConfig(this.getRelativePath(), true);

            this.getInstance().getCachedConfigs().put(fileName, guiConfig);
        }

        // Set up the context
        // Get the default gui details
        this.getGuiContext().setDefaultGuiDetails(guiConfig.getGuiDetails(
                Optional.empty(),
                this.getConsumers()
        ));

        this.setId(
                this.getInstance()
                        .getGuiFactory()
                        .create(
                                this.getGuiContext().getDefaultGuiDetails()
                        )
        );

        // Register the open and close actions
        guiConfig.registerOpenCloseActions(
                this.getId(),
                getGuiContext().getGuiFactory()
        );

    }

}