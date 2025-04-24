package it.mikeslab.econexuslite.bootstrap;

import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.econexuslite.pojo.Banknote;
import it.mikeslab.econexuslite.pojo.ConfigStructure;

public class BanknoteLoader extends Loader<Banknote> {


    public BanknoteLoader(Configurable config, ConfigStructure structure) {
        super(config, structure);
    }
}
