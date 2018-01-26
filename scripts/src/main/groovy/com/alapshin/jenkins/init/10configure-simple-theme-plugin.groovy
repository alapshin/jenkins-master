package main.groovy.com.alapshin.jenkins.init

import hudson.model.PageDecorator
import jenkins.model.Jenkins
import org.yaml.snakeyaml.Yaml
import org.codefirst.SimpleThemeDecorator

import java.util.logging.Logger

logger = Logger.getLogger("")
config = new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream {
    return new Yaml().load(it)
}

if (!config.theme) {
    logger.info('Nothing changed. No theme settings are found.')
    return
}

for (pd in PageDecorator.all()) {
    if (pd instanceof SimpleThemeDecorator) {
        pd.cssUrl = config.theme.css
        pd.save()
        logger.info("Setting theme settings " + config.theme)
    }
}

