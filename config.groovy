import jenkins.model.*
import org.yaml.snakeyaml.Yaml

new File(Jenkins.instance.getRootDir(), 
    "init.groovy.d/config.yml").withInputStream { 
        return new Yaml().load(it)
}
